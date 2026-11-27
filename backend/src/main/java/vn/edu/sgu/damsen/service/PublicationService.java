package vn.edu.sgu.damsen.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.damsen.domain.*;
import vn.edu.sgu.damsen.repository.*;
import vn.edu.sgu.damsen.web.dto.Dtos.*;

/**
 * Xuất bản an toàn (UC12 / Story 2.4): đổi con trỏ publications sang phiên bản mới trong MỘT transaction,
 * phiên bản cũ chuyển ARCHIVED. Bản en/ko đang phục vụ vẫn giữ nguyên khi bản Việt đổi ("Cần cập nhật").
 */
@Service
@Transactional
public class PublicationService {
    private final PublicationRepository publications;
    private final NarrationVersionRepository versions;
    private final AudioAssetRepository audios;
    private final PoiRepository pois;
    private final ZoneRepository zones;
    private final ProcessingJobRepository jobs;
    private final NarrationService narration;
    private final AudioStorage storage;

    public PublicationService(PublicationRepository publications, NarrationVersionRepository versions,
                              AudioAssetRepository audios, PoiRepository pois, ZoneRepository zones,
                              ProcessingJobRepository jobs, NarrationService narration, AudioStorage storage) {
        this.publications = publications;
        this.versions = versions;
        this.audios = audios;
        this.pois = pois;
        this.zones = zones;
        this.jobs = jobs;
        this.narration = narration;
        this.storage = storage;
    }

    public void publish(Long poiId, PublishRequest r) {
        narration.requirePoi(poiId);
        NarrationVersion v = narration.requireVersion(r.versionId());
        if (!v.getPoiId().equals(poiId) || !v.getLanguageCode().equals(r.languageCode())) {
            throw ApiException.badRequest("Phiên bản không thuộc điểm/ngôn ngữ này");
        }
        if (!v.isApproved()) throw ApiException.conflict("Chỉ xuất bản được phiên bản đã duyệt (APPROVED)");
        if (!Language.SOURCE.equals(r.languageCode())
                && publications.findByLanguageCodeAndPoiId(Language.SOURCE, poiId).isEmpty()) {
            throw ApiException.conflict("Hãy xuất bản bản tiếng Việt trước khi xuất bản ngôn ngữ khác");
        }

        // Chọn audio: theo yêu cầu, hoặc tự lấy audio READY mới nhất khớp văn bản (text_hash = content_hash)
        AudioAsset audio;
        if (r.audioAssetId() != null) {
            audio = audios.findById(r.audioAssetId()).orElseThrow(() -> ApiException.notFound("file âm thanh"));
            if (!audio.getNarrationVersionId().equals(v.getId())) {
                throw ApiException.badRequest("File âm thanh không thuộc phiên bản này");
            }
            if (!audio.isReady()) throw ApiException.conflict("File âm thanh chưa sẵn sàng");
            if (!Objects.equals(audio.getTextHash(), v.getContentHash())) {
                throw ApiException.conflict("File âm thanh không khớp văn bản (text_hash khác content_hash)");
            }
        } else {
            audio = audios.findByNarrationVersionIdOrderByIdDesc(v.getId()).stream()
                    .filter(a -> a.isReady() && Objects.equals(a.getTextHash(), v.getContentHash()))
                    .filter(a -> storage.exists(a.getStorageKey()))
                    .findFirst().orElse(null);
        }

        Publication pub = publications.findByLanguageCodeAndPoiId(r.languageCode(), poiId)
                .orElseGet(() -> new Publication(r.languageCode(), poiId));
        Long oldVersionId = pub.getNarrationVersionId();
        if (oldVersionId != null && !oldVersionId.equals(v.getId())) {
            versions.findById(oldVersionId).ifPresent(old -> old.setStatus(NarrationVersion.ARCHIVED));
        }
        pub.setNarrationVersionId(v.getId());
        pub.setAudioAssetId(audio == null ? null : audio.getId());
        pub.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
        publications.save(pub);
    }

    /** Gỡ xuất bản một ngôn ngữ: khách sẽ nhận bản tiếng Việt. */
    public void unpublish(Long poiId, String lang) {
        Publication pub = publications.findByLanguageCodeAndPoiId(lang, poiId)
                .orElseThrow(() -> ApiException.notFound("bản xuất bản của ngôn ngữ " + lang));
        if (Language.SOURCE.equals(lang) && publications.findByPoiId(poiId).size() > 1) {
            throw ApiException.conflict("Hãy gỡ các ngôn ngữ khác trước khi gỡ bản tiếng Việt (bản dự phòng)");
        }
        publications.delete(pub);
    }

    @Transactional(readOnly = true)
    public OverviewDto overview(boolean machineTranslationEnabled) {
        List<Poi> allPois = pois.findAll();
        List<Publication> pubs = publications.findAll();
        Map<Long, NarrationVersion> vById = versions.findAllById(
                pubs.stream().map(Publication::getNarrationVersionId).toList()).stream()
                .collect(Collectors.toMap(NarrationVersion::getId, x -> x));
        Map<Long, String> poiCode = allPois.stream().collect(Collectors.toMap(Poi::getId, Poi::getCode));
        List<NeedsUpdateItem> needs = new ArrayList<>();
        pubs.stream().collect(Collectors.groupingBy(Publication::getPoiId)).forEach((poiId, list) ->
                CatalogAdminService.needsUpdate(list, vById)
                        .forEach(lang -> needs.add(new NeedsUpdateItem(poiId, poiCode.get(poiId), lang))));
        needs.sort(Comparator.comparing(NeedsUpdateItem::poiCode).thenComparing(NeedsUpdateItem::languageCode));
        List<LangCount> byLang = pubs.stream().collect(Collectors.groupingBy(Publication::getLanguageCode, TreeMap::new,
                Collectors.counting())).entrySet().stream().map(e -> new LangCount(e.getKey(), e.getValue())).toList();
        List<LangCount> byStatus = jobs.findAll().stream().collect(Collectors.groupingBy(ProcessingJob::getStatus,
                TreeMap::new, Collectors.counting())).entrySet().stream()
                .map(e -> new LangCount(e.getKey(), e.getValue())).toList();
        return new OverviewDto(allPois.size(), allPois.stream().filter(Poi::isActive).count(), zones.count(),
                byLang, needs, byStatus, machineTranslationEnabled);
    }
}
