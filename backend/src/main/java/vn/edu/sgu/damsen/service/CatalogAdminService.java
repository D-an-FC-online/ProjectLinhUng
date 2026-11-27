package vn.edu.sgu.damsen.service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.damsen.domain.*;
import vn.edu.sgu.damsen.repository.*;
import vn.edu.sgu.damsen.web.dto.Dtos.*;

/** Quản lý khu vực, điểm tham quan (UC07) và bảng thuật ngữ (UC14). */
@Service
@Transactional
public class CatalogAdminService {
    private final ZoneRepository zones;
    private final PoiRepository pois;
    private final GlossaryRepository glossary;
    private final PublicationRepository publications;
    private final NarrationVersionRepository versions;

    public CatalogAdminService(ZoneRepository zones, PoiRepository pois, GlossaryRepository glossary,
                               PublicationRepository publications, NarrationVersionRepository versions) {
        this.zones = zones;
        this.pois = pois;
        this.glossary = glossary;
        this.publications = publications;
        this.versions = versions;
    }

    /* ----------------------------- Zones ----------------------------- */

    @Transactional(readOnly = true)
    public List<ZoneAdminDto> listZones() {
        Map<Long, Long> counts = pois.findAll().stream()
                .collect(Collectors.groupingBy(Poi::getZoneId, Collectors.counting()));
        return zones.findAll().stream()
                .sorted(Comparator.comparing(Zone::getCode))
                .map(z -> new ZoneAdminDto(z.getId(), z.getCode(), z.getNameVi(), z.getNameEn(), z.getNameKo(),
                        z.isActive(), counts.getOrDefault(z.getId(), 0L)))
                .toList();
    }

    public Long saveZone(Long id, ZoneRequest r) {
        Zone z = id == null ? new Zone() : zones.findById(id).orElseThrow(() -> ApiException.notFound("khu vực"));
        if (!r.code().equals(z.getCode()) && zones.existsByCode(r.code())) {
            throw ApiException.conflict("Mã khu vực " + r.code() + " đã tồn tại");
        }
        z.setCode(r.code());
        z.setNameVi(r.nameVi().trim());
        z.setNameEn(blankToNull(r.nameEn()));
        z.setNameKo(blankToNull(r.nameKo()));
        z.setActive(r.active());
        return zones.save(z).getId();
    }

    /* ------------------------------ POIs ------------------------------ */

    @Transactional(readOnly = true)
    public List<PoiAdminDto> listPois() {
        Map<Long, Zone> zoneById = zones.findAll().stream().collect(Collectors.toMap(Zone::getId, z -> z));
        List<Publication> allPubs = publications.findAll();
        Map<Long, List<Publication>> pubsByPoi = allPubs.stream().collect(Collectors.groupingBy(Publication::getPoiId));
        Map<Long, NarrationVersion> vById = versions.findAllById(
                allPubs.stream().map(Publication::getNarrationVersionId).toList()).stream()
                .collect(Collectors.toMap(NarrationVersion::getId, v -> v));
        return pois.findAllByOrderByCode().stream()
                .map(p -> toDto(p, zoneById.get(p.getZoneId()), pubsByPoi.getOrDefault(p.getId(), List.of()), vById))
                .toList();
    }

    @Transactional(readOnly = true)
    public PoiAdminDto getPoi(Long id) {
        Poi p = pois.findById(id).orElseThrow(() -> ApiException.notFound("điểm tham quan"));
        List<Publication> pubs = publications.findByPoiId(id);
        Map<Long, NarrationVersion> vById = versions.findAllById(
                pubs.stream().map(Publication::getNarrationVersionId).toList()).stream()
                .collect(Collectors.toMap(NarrationVersion::getId, v -> v));
        return toDto(p, zones.findById(p.getZoneId()).orElse(null), pubs, vById);
    }

    public Long savePoi(Long id, PoiRequest r) {
        Poi p = id == null ? new Poi() : pois.findById(id).orElseThrow(() -> ApiException.notFound("điểm tham quan"));
        if (!zones.existsById(r.zoneId())) throw ApiException.badRequest("Khu vực không tồn tại");
        if (!r.code().equals(p.getCode()) && pois.existsByCode(r.code())) {
            throw ApiException.conflict("Mã điểm " + r.code() + " đã tồn tại");
        }
        p.setZoneId(r.zoneId());
        p.setCode(r.code());
        p.setLatitude(r.latitude());
        p.setLongitude(r.longitude());
        p.setRadiusM(r.radiusM());
        p.setStatus(r.status());
        return pois.save(p).getId();
    }

    /** Trạng thái "Cần cập nhật" được suy ra lúc truy vấn (Story 2.4), không lưu thành cột. */
    static List<String> needsUpdate(List<Publication> pubs, Map<Long, NarrationVersion> vById) {
        Long viVersion = pubs.stream().filter(pb -> Language.SOURCE.equals(pb.getLanguageCode()))
                .map(Publication::getNarrationVersionId).findFirst().orElse(null);
        if (viVersion == null) return List.of();
        return pubs.stream()
                .filter(pb -> !Language.SOURCE.equals(pb.getLanguageCode()))
                .filter(pb -> {
                    NarrationVersion v = vById.get(pb.getNarrationVersionId());
                    return v != null && !viVersion.equals(v.getSourceVersionId());
                })
                .map(Publication::getLanguageCode).sorted().toList();
    }

    private PoiAdminDto toDto(Poi p, Zone z, List<Publication> pubs, Map<Long, NarrationVersion> vById) {
        String titleVi = pubs.stream().filter(pb -> Language.SOURCE.equals(pb.getLanguageCode()))
                .map(pb -> vById.get(pb.getNarrationVersionId())).filter(Objects::nonNull)
                .map(NarrationVersion::getTitle).findFirst().orElse(null);
        return new PoiAdminDto(p.getId(), p.getZoneId(), z == null ? null : z.getCode(), p.getCode(),
                p.getLatitude(), p.getLongitude(), p.getRadiusM(), p.getStatus(), titleVi,
                pubs.stream().map(Publication::getLanguageCode).sorted().toList(),
                needsUpdate(pubs, vById));
    }

    /* ---------------------------- Glossary ---------------------------- */

    @Transactional(readOnly = true)
    public List<GlossaryDto> listGlossary() {
        return glossary.findAllByOrderByTerm().stream().map(CatalogAdminService::toDto).toList();
    }

    public Long saveGlossary(Long id, GlossaryDto r) {
        GlossaryTerm g = id == null ? new GlossaryTerm()
                : glossary.findById(id).orElseThrow(() -> ApiException.notFound("thuật ngữ"));
        String term = r.term().trim();
        if (!term.equals(g.getTerm()) && glossary.existsByTerm(term)) {
            throw ApiException.conflict("Thuật ngữ \"" + term + "\" đã có trong bảng");
        }
        g.setTerm(term);
        g.setTermType(r.termType());
        g.setKeepOriginal(r.keepOriginal());
        g.setTranslationEn(blankToNull(r.translationEn()));
        g.setTranslationKo(blankToNull(r.translationKo()));
        g.setTtsAliasVi(blankToNull(r.ttsAliasVi()));
        g.setTtsAliasEn(blankToNull(r.ttsAliasEn()));
        g.setTtsAliasKo(blankToNull(r.ttsAliasKo()));
        g.setNote(blankToNull(r.note()));
        return glossary.save(g).getId();
    }

    public void deleteGlossary(Long id) {
        if (!glossary.existsById(id)) throw ApiException.notFound("thuật ngữ");
        glossary.deleteById(id);
    }

    static GlossaryDto toDto(GlossaryTerm g) {
        return new GlossaryDto(g.getId(), g.getTerm(), g.getTermType(), g.isKeepOriginal(),
                g.getTranslationEn(), g.getTranslationKo(), g.getTtsAliasVi(), g.getTtsAliasEn(),
                g.getTtsAliasKo(), g.getNote());
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
