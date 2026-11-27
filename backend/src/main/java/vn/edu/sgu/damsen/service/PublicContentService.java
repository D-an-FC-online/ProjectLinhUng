package vn.edu.sgu.damsen.service;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.damsen.config.AppProperties;
import vn.edu.sgu.damsen.domain.*;
import vn.edu.sgu.damsen.repository.*;
import vn.edu.sgu.damsen.web.dto.Dtos.*;

/**
 * Nội dung phục vụ khách tham quan (UC01-UC05). Chỉ trả về nội dung đang được xuất bản
 * (có dòng trong bảng publications). Thiếu ngôn ngữ yêu cầu thì fallback về tiếng Việt.
 * Gồm cả NearbySearch: tính khoảng cách Haversine, sắp xếp gần -> xa, gom nhóm điểm sát nhau.
 */
@Service
@Transactional(readOnly = true)
public class PublicContentService {

    private final LanguageRepository languages;
    private final ZoneRepository zones;
    private final PoiRepository pois;
    private final PublicationRepository publications;
    private final NarrationVersionRepository versions;
    private final AudioAssetRepository audios;
    private final AppProperties props;

    public PublicContentService(LanguageRepository languages, ZoneRepository zones, PoiRepository pois,
                                PublicationRepository publications, NarrationVersionRepository versions,
                                AudioAssetRepository audios, AppProperties props) {
        this.languages = languages;
        this.zones = zones;
        this.pois = pois;
        this.publications = publications;
        this.versions = versions;
        this.audios = audios;
        this.props = props;
    }

    /** Nội dung đang phục vụ cho một POI. */
    private record Served(Poi poi, Zone zone, Publication publication, NarrationVersion version,
                          String language, boolean fallback) {}

    public List<LanguageDto> languages() {
        return languages.findAll().stream()
                .sorted(Comparator.comparing((Language l) -> !l.isSource()).thenComparing(Language::getCode))
                .map(l -> new LanguageDto(l.getCode(), l.getName(), l.isSource()))
                .toList();
    }

    public List<ZoneDto> zones(String lang) {
        String l = normalizeLang(lang);
        return zones.findByActiveTrueOrderByCode().stream()
                .map(z -> new ZoneDto(z.getId(), z.getCode(), z.nameFor(l)))
                .toList();
    }

    /** UC03: duyệt thủ công theo khu vực / từ khóa, có phân trang. */
    public PageDto<PoiSummary> list(Long zoneId, String q, String lang, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        String keyword = fold(q);
        List<PoiSummary> all = servedAll(normalizeLang(lang)).stream()
                .filter(s -> zoneId == null || zoneId.equals(s.poi().getZoneId()))
                .filter(s -> keyword.isEmpty()
                        || fold(s.version().getTitle()).contains(keyword)
                        || fold(s.poi().getCode()).contains(keyword)
                        || fold(s.zone().nameFor(s.language())).contains(keyword))
                .sorted(Comparator.comparing((Served s) -> s.version().getTitle(), String.CASE_INSENSITIVE_ORDER))
                .map(s -> summary(s, null, null))
                .toList();
        int from = Math.min(safePage * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return new PageDto<>(all.subList(from, to), safePage, safeSize, all.size());
    }

    /** UC02: các điểm trong bán kính, sắp xếp gần -> xa; điểm cách nhau < 20m chung một nhóm. */
    public List<PoiSummary> nearby(double lat, double lng, Integer radiusM, String lang) {
        int radius = radiusM == null ? props.nearby().defaultRadiusM() : Math.min(Math.max(radiusM, 10), 2000);
        List<Served> served = servedAll(normalizeLang(lang));

        record Hit(Served s, double d) {}
        List<Hit> hits = served.stream()
                .map(s -> new Hit(s, GeoUtil.distanceM(lat, lng,
                        s.poi().getLatitude().doubleValue(), s.poi().getLongitude().doubleValue())))
                .filter(h -> h.d() <= radius)
                .sorted(Comparator.comparingDouble(Hit::d))
                .toList();

        // Gom nhóm (union-find đơn giản): hai điểm cách nhau < group-distance-m thì chung nhóm
        int n = hits.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        Function<Integer, Integer> find = new Function<>() {
            public Integer apply(Integer i) {
                while (parent[i] != i) { parent[i] = parent[parent[i]]; i = parent[i]; }
                return i;
            }
        };
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Poi a = hits.get(i).s().poi(), b = hits.get(j).s().poi();
                double d = GeoUtil.distanceM(a.getLatitude().doubleValue(), a.getLongitude().doubleValue(),
                        b.getLatitude().doubleValue(), b.getLongitude().doubleValue());
                if (d < props.nearby().groupDistanceM()) parent[find.apply(i)] = find.apply(j);
            }
        }
        Map<Integer, Integer> groupSize = new HashMap<>();
        for (int i = 0; i < n; i++) groupSize.merge(find.apply(i), 1, Integer::sum);
        // Đánh số nhóm theo thứ tự xuất hiện (gần nhất trước); điểm đứng một mình thì groupId = null
        Map<Integer, Integer> groupNo = new HashMap<>();
        List<PoiSummary> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int root = find.apply(i);
            Integer gid = groupSize.get(root) > 1 ? groupNo.computeIfAbsent(root, r -> groupNo.size() + 1) : null;
            result.add(summary(hits.get(i).s(), (int) Math.round(hits.get(i).d()), gid));
        }
        return result;
    }

    /** UC04/UC05: xem nội dung và lấy đường dẫn audio của một điểm. */
    public PoiDetail detail(Long poiId, String lang) {
        String requested = normalizeLang(lang);
        Served s = served(poiId, requested).orElseThrow(() -> ApiException.notFound("nội dung của điểm này"));
        Publication pub = s.publication();
        String audioUrl = pub.getAudioAssetId() == null ? null : "/api/v1/public/audio/" + pub.getAudioAssetId();
        Poi p = s.poi();
        return new PoiDetail(p.getId(), p.getCode(), p.getZoneId(), s.zone().nameFor(s.language()),
                p.getLatitude().doubleValue(), p.getLongitude().doubleValue(), p.getRadiusM(),
                s.version().getTitle(), s.version().getBody(), s.language(), requested, s.fallback(),
                audioUrl, s.version().getId(), pub.getPublishedAt());
    }

    /** Chỉ cho phép tải file audio đang được xuất bản. */
    public AudioAsset publishedAudio(Long audioId) {
        AudioAsset asset = audios.findById(audioId).orElseThrow(() -> ApiException.notFound("file âm thanh"));
        if (!asset.isReady() || !publications.existsByAudioAssetId(audioId)) {
            throw ApiException.notFound("file âm thanh");
        }
        return asset;
    }

    /* ------------------------------------------------------------------ */

    private Optional<Served> served(Long poiId, String lang) {
        Poi poi = pois.findById(poiId).filter(Poi::isActive).orElse(null);
        if (poi == null) return Optional.empty();
        Zone zone = zones.findById(poi.getZoneId()).filter(Zone::isActive).orElse(null);
        if (zone == null) return Optional.empty();
        Optional<Publication> own = publications.findByLanguageCodeAndPoiId(lang, poiId);
        Publication pub = own.or(() -> publications.findByLanguageCodeAndPoiId(Language.SOURCE, poiId)).orElse(null);
        if (pub == null) return Optional.empty();
        NarrationVersion v = versions.findById(pub.getNarrationVersionId()).orElseThrow();
        return Optional.of(new Served(poi, zone, pub, v, pub.getLanguageCode(), own.isEmpty()));
    }

    private List<Served> servedAll(String lang) {
        List<Poi> visible = pois.findVisible();
        if (visible.isEmpty()) return List.of();
        Map<Long, Zone> zoneById = zones.findAll().stream().collect(Collectors.toMap(Zone::getId, z -> z));
        Map<String, Publication> pubs = publications.findByLanguageCodeIn(List.of(lang, Language.SOURCE)).stream()
                .collect(Collectors.toMap(p -> p.getLanguageCode() + ":" + p.getPoiId(), p -> p));
        Map<Long, NarrationVersion> vById = versions.findAllById(
                pubs.values().stream().map(Publication::getNarrationVersionId).toList()).stream()
                .collect(Collectors.toMap(NarrationVersion::getId, v -> v));
        List<Served> out = new ArrayList<>();
        for (Poi p : visible) {
            Publication own = pubs.get(lang + ":" + p.getId());
            Publication pub = own != null ? own : pubs.get(Language.SOURCE + ":" + p.getId());
            if (pub == null) continue; // chưa xuất bản -> khách không thấy
            out.add(new Served(p, zoneById.get(p.getZoneId()), pub, vById.get(pub.getNarrationVersionId()),
                    pub.getLanguageCode(), own == null));
        }
        return out;
    }

    private PoiSummary summary(Served s, Integer distance, Integer groupId) {
        Poi p = s.poi();
        return new PoiSummary(p.getId(), p.getCode(), p.getZoneId(), s.zone().nameFor(s.language()),
                p.getLatitude().doubleValue(), p.getLongitude().doubleValue(), p.getRadiusM(),
                s.version().getTitle(), s.language(), s.fallback(), s.publication().getAudioAssetId() != null,
                distance, groupId);
    }

    static String normalizeLang(String lang) {
        if (lang == null) return Language.SOURCE;
        String l = lang.trim().toLowerCase(Locale.ROOT);
        if (l.length() > 2) l = l.substring(0, 2);
        return l.matches("[a-z]{2}") ? l : Language.SOURCE;
    }

    /** Bỏ dấu tiếng Việt + chữ thường để tìm kiếm không phân biệt dấu. */
    static String fold(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }
}
