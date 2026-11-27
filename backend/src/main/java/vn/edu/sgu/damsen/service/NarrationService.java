package vn.edu.sgu.damsen.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.sgu.damsen.domain.*;
import vn.edu.sgu.damsen.repository.*;
import vn.edu.sgu.damsen.web.dto.Dtos.*;

/**
 * Biên soạn và vòng đời phiên bản thuyết minh (UC08, UC10, UC12) + tải audio thủ công (UC11).
 * Vòng đời: DRAFT -> APPROVED (bị khóa, muốn sửa phải tạo bản nháp mới) -> ARCHIVED (khi bị thay thế).
 */
@Service
@Transactional
public class NarrationService {
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:[.,]\\d{3})*(?:[.,]\\d+)?");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PoiRepository pois;
    private final LanguageRepository languages;
    private final NarrationVersionRepository versions;
    private final AudioAssetRepository audios;
    private final PublicationRepository publications;
    private final GlossaryRepository glossary;
    private final AudioStorage storage;

    public NarrationService(PoiRepository pois, LanguageRepository languages, NarrationVersionRepository versions,
                            AudioAssetRepository audios, PublicationRepository publications,
                            GlossaryRepository glossary, AudioStorage storage) {
        this.pois = pois;
        this.languages = languages;
        this.versions = versions;
        this.audios = audios;
        this.publications = publications;
        this.glossary = glossary;
        this.storage = storage;
    }

    /* =========================== Xem nội dung =========================== */

    @Transactional(readOnly = true)
    public List<LanguageContentDto> content(Long poiId) {
        requirePoi(poiId);
        List<NarrationVersion> all = versions.findByPoiIdOrderByLanguageCodeAscVersionNoDesc(poiId);
        Map<Long, List<AudioAsset>> audioByVersion = all.isEmpty() ? Map.of()
                : audios.findByNarrationVersionIdIn(all.stream().map(NarrationVersion::getId).toList()).stream()
                .collect(Collectors.groupingBy(AudioAsset::getNarrationVersionId));
        List<Publication> pubs = publications.findByPoiId(poiId);
        Map<Long, NarrationVersion> vById = all.stream().collect(Collectors.toMap(NarrationVersion::getId, v -> v));
        List<String> needsUpdate = CatalogAdminService.needsUpdate(pubs, vById);
        Set<Long> publishedIds = pubs.stream().map(Publication::getNarrationVersionId).collect(Collectors.toSet());

        return languages.findAll().stream()
                .sorted(Comparator.comparing((Language l) -> !l.isSource()).thenComparing(Language::getCode))
                .map(l -> {
                    Publication pub = pubs.stream().filter(p -> p.getLanguageCode().equals(l.getCode()))
                            .findFirst().orElse(null);
                    PublicationDto pubDto = pub == null ? null : new PublicationDto(l.getCode(),
                            pub.getNarrationVersionId(), pub.getAudioAssetId(), pub.getPublishedAt(),
                            needsUpdate.contains(l.getCode()));
                    List<VersionDto> vs = all.stream().filter(v -> v.getLanguageCode().equals(l.getCode()))
                            .map(v -> toDto(v, publishedIds.contains(v.getId()),
                                    audioByVersion.getOrDefault(v.getId(), List.of())))
                            .toList();
                    return new LanguageContentDto(l.getCode(), l.getName(), pubDto, vs);
                }).toList();
    }

    @Transactional(readOnly = true)
    public VersionDto version(Long id) {
        NarrationVersion v = requireVersion(id);
        return toDto(v, publications.findByNarrationVersionId(id).isPresent(),
                audios.findByNarrationVersionIdOrderByIdDesc(id));
    }

    /* ============================ Bản nháp ============================ */

    /** Tạo bản nháp mới (tiếng Việt = bản gốc; en/ko = bản dịch nhập tay, trỏ về bản Việt hiện hành). */
    public Long createDraft(Long poiId, DraftRequest r) {
        requirePoi(poiId);
        if (!languages.existsById(r.languageCode())) throw ApiException.badRequest("Ngôn ngữ không được hỗ trợ");
        Long source = Language.SOURCE.equals(r.languageCode()) ? null : currentSourceVersion(poiId).getId();
        return newVersion(poiId, r.languageCode(), source, r.title(), r.body()).getId();
    }

    /** Sửa bản nháp. Bản đã duyệt bị khóa (Story 2.1). */
    public void updateDraft(Long id, DraftUpdateRequest r) {
        NarrationVersion v = requireVersion(id);
        if (!v.isDraft()) {
            throw ApiException.conflict("Phiên bản đã duyệt bị khóa. Hãy bấm \"Tạo bản nháp mới\" để chỉnh sửa.");
        }
        v.setTitle(r.title().trim());
        v.setBody(r.body().trim());
        v.setContentHash(HashUtil.contentHash(v.getTitle(), v.getBody()));
    }

    /** Sao chép một phiên bản thành bản nháp mới để sửa. Bản dịch sẽ trỏ về bản Việt hiện hành. */
    public Long copyToDraft(Long id) {
        NarrationVersion v = requireVersion(id);
        Long source = Language.SOURCE.equals(v.getLanguageCode()) ? null
                : currentSourceVersion(v.getPoiId()).getId();
        return newVersion(v.getPoiId(), v.getLanguageCode(), source, v.getTitle(), v.getBody()).getId();
    }

    /** Bỏ bản nháp (chuyển ARCHIVED, không xóa để giữ lịch sử). */
    public void discardDraft(Long id) {
        NarrationVersion v = requireVersion(id);
        if (!v.isDraft()) throw ApiException.conflict("Chỉ bỏ được bản nháp");
        v.setStatus(NarrationVersion.ARCHIVED);
    }

    public NarrationVersion newVersion(Long poiId, String lang, Long sourceVersionId, String title, String body) {
        NarrationVersion v = new NarrationVersion();
        v.setPoiId(poiId);
        v.setLanguageCode(lang);
        v.setVersionNo(versions.maxVersionNo(poiId, lang) + 1);
        v.setSourceVersionId(sourceVersionId);
        v.setTitle(title.trim());
        v.setBody(body.trim());
        v.setStatus(NarrationVersion.DRAFT);
        v.setContentHash(HashUtil.contentHash(v.getTitle(), v.getBody()));
        return versions.save(v);
    }

    /** Bản tiếng Việt làm nguồn: ưu tiên bản đang xuất bản, nếu chưa có thì bản đã duyệt mới nhất. */
    @Transactional(readOnly = true)
    public NarrationVersion currentSourceVersion(Long poiId) {
        return publications.findByLanguageCodeAndPoiId(Language.SOURCE, poiId)
                .flatMap(p -> versions.findById(p.getNarrationVersionId()))
                .or(() -> versions.findByPoiIdOrderByLanguageCodeAscVersionNoDesc(poiId).stream()
                        .filter(v -> Language.SOURCE.equals(v.getLanguageCode()) && v.isApproved())
                        .findFirst())
                .orElseThrow(() -> ApiException.conflict(
                        "Điểm này chưa có bản tiếng Việt đã duyệt. Hãy duyệt bản tiếng Việt trước."));
    }

    /* ======================== Kiểm tra (QA) & duyệt ======================== */

    /** QA tự động (Story 2.2). Lỗi ERROR chặn duyệt, WARNING chỉ cảnh báo. */
    @Transactional(readOnly = true)
    public QaResult qa(Long id) {
        NarrationVersion v = requireVersion(id);
        List<QaIssue> issues = new ArrayList<>();
        if (v.getTitle() == null || v.getTitle().isBlank()) issues.add(err("Tiêu đề đang trống"));
        if (v.getBody() == null || v.getBody().isBlank()) issues.add(err("Nội dung đang trống"));
        String text = v.getTitle() + "\n" + v.getBody();
        if (text.contains("notranslate") || text.contains("<span") || text.contains("⟦")) {
            issues.add(err("Còn sót mã giữ chỗ thuật ngữ (thẻ <span>/⟦ ⟧) trong văn bản"));
        }

        String srcTitle = null, srcBody = null;
        if (!Language.SOURCE.equals(v.getLanguageCode())) {
            NarrationVersion src = v.getSourceVersionId() == null ? null
                    : versions.findById(v.getSourceVersionId()).orElse(null);
            if (src == null) {
                issues.add(err("Bản dịch không có bản gốc tiếng Việt (source_version_id)"));
            } else {
                srcTitle = src.getTitle();
                srcBody = src.getBody();
                String srcText = src.getTitle() + "\n" + src.getBody();
                for (GlossaryTerm g : glossary.findAll()) {
                    if (!g.isKeepOriginal() && g.getTranslationEn() == null && g.getTranslationKo() == null) continue;
                    if (!srcText.contains(g.getTerm())) continue;
                    String expected = g.translationFor(v.getLanguageCode());
                    if (!text.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT))) {
                        issues.add(g.isKeepOriginal()
                                ? err("Thuật ngữ \"" + g.getTerm() + "\" phải được viết là \"" + expected + "\" trong bản dịch")
                                : warn("Nên dịch \"" + g.getTerm() + "\" thành \"" + expected + "\" theo bảng thuật ngữ"));
                    }
                }
                Set<String> missing = new TreeSet<>(numbers(srcText));
                missing.removeAll(numbers(text));
                if (!missing.isEmpty()) {
                    issues.add(warn("Các con số có trong bản gốc nhưng không thấy trong bản dịch: " + String.join(", ", missing)));
                }
                double ratio = (double) v.getBody().length() / Math.max(1, src.getBody().length());
                if (ratio < 0.4 || ratio > 2.5) {
                    issues.add(warn(String.format(Locale.ROOT,
                            "Độ dài bản dịch bất thường (bằng %.0f%% bản gốc)", ratio * 100)));
                }
                if (NarrationVersion.ARCHIVED.equals(src.getStatus())) {
                    issues.add(warn("Bản gốc tiếng Việt của bản dịch này đã cũ (ARCHIVED)"));
                }
            }
        }
        boolean canApprove = v.isDraft() && issues.stream().noneMatch(i -> "ERROR".equals(i.level()));
        return new QaResult(canApprove, issues, srcTitle, srcBody);
    }

    public void approve(Long id) {
        NarrationVersion v = requireVersion(id);
        if (!v.isDraft()) throw ApiException.conflict("Chỉ duyệt được bản nháp (DRAFT)");
        QaResult qa = qa(id);
        if (!qa.canApprove()) {
            throw ApiException.conflict("Không thể duyệt vì còn lỗi nghiêm trọng: " + qa.issues().stream()
                    .filter(i -> "ERROR".equals(i.level())).map(QaIssue::message).collect(Collectors.joining("; ")));
        }
        v.setStatus(NarrationVersion.APPROVED);
    }

    /* ============================ Audio ============================ */

    /** Tải file âm thanh thủ công cho một phiên bản đã duyệt. */
    public Long uploadAudio(Long versionId, MultipartFile file) {
        NarrationVersion v = requireVersion(versionId);
        if (!v.isApproved()) throw ApiException.conflict("Chỉ gắn audio cho phiên bản đã duyệt (APPROVED)");
        if (file == null || file.isEmpty()) throw ApiException.badRequest("Chưa chọn file");
        String ext = AudioStorage.extensionOf(file.getOriginalFilename());
        if (!AudioStorage.isSupportedExtension(ext)) throw ApiException.badRequest("Chỉ nhận file .mp3, .m4a, .ogg, .wav");
        try {
            return saveAudio(v, file.getBytes(), ext, "UPLOAD");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Long saveAudio(NarrationVersion v, byte[] data, String ext, String source) {
        String key = "nv%d_%s_%s.%s".formatted(v.getId(), v.getLanguageCode(),
                LocalDateTime.now(ZoneOffset.UTC).format(FILE_TS) + "_" + UUID.randomUUID().toString().substring(0, 6), ext);
        storage.write(key, data);
        AudioAsset a = new AudioAsset();
        a.setNarrationVersionId(v.getId());
        a.setStorageKey(key);
        a.setStatus("READY");
        a.setSource(source);
        a.setTextHash(v.getContentHash());
        a.setContentHash(HashUtil.sha256(data));
        return audios.save(a).getId();
    }

    @Transactional(readOnly = true)
    public AudioAsset audio(Long id) {
        return audios.findById(id).orElseThrow(() -> ApiException.notFound("file âm thanh"));
    }

    /* ============================ Helpers ============================ */

    private VersionDto toDto(NarrationVersion v, boolean published, List<AudioAsset> as) {
        List<AudioDto> audioDtos = as.stream()
                .sorted(Comparator.comparing(AudioAsset::getId).reversed())
                .map(a -> new AudioDto(a.getId(), a.getStorageKey(), a.getStatus(), a.getSource(),
                        Objects.equals(a.getTextHash(), v.getContentHash()), a.getCreatedAt(),
                        "/api/v1/admin/audio/" + a.getId()))
                .toList();
        return new VersionDto(v.getId(), v.getLanguageCode(), v.getVersionNo(), v.getStatus(), v.getSourceVersionId(),
                v.getTitle(), v.getBody(), v.getContentHash(), v.getCreatedAt(), published, audioDtos);
    }

    public Poi requirePoi(Long poiId) {
        return pois.findById(poiId).orElseThrow(() -> ApiException.notFound("điểm tham quan"));
    }

    public NarrationVersion requireVersion(Long id) {
        return versions.findById(id).orElseThrow(() -> ApiException.notFound("phiên bản thuyết minh"));
    }

    private static Set<String> numbers(String s) {
        Set<String> out = new HashSet<>();
        Matcher m = NUMBER.matcher(s);
        while (m.find()) out.add(m.group().replaceAll("[.,](?=\\d{3}(\\D|$))", ""));
        return out;
    }

    private static QaIssue err(String m) { return new QaIssue("ERROR", m); }
    private static QaIssue warn(String m) { return new QaIssue("WARNING", m); }
}
