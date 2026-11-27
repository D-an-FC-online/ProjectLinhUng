package vn.edu.sgu.damsen.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Các đối tượng dữ liệu (JSON) trao đổi giữa frontend và API. */
public final class Dtos {
    private Dtos() {}

    /* ===================== Public (khách tham quan) ===================== */

    public record LanguageDto(String code, String name, boolean source) {}

    public record ZoneDto(Long id, String code, String name) {}

    public record PoiSummary(
            Long id, String code, Long zoneId, String zoneName,
            double latitude, double longitude, int radiusM,
            String title, String language, boolean fallback, boolean hasAudio,
            Integer distanceM, Integer groupId) {}

    public record PoiDetail(
            Long id, String code, Long zoneId, String zoneName,
            double latitude, double longitude, int radiusM,
            String title, String body, String language, String requestedLanguage, boolean fallback,
            String audioUrl, Long versionId, LocalDateTime publishedAt) {}

    public record PageDto<T>(List<T> items, int page, int size, long total) {}

    /* ===================== Auth ===================== */

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record MeDto(String username, List<String> roles) {}

    public record ChangePasswordRequest(@NotBlank String oldPassword, @NotBlank @Size(min = 8, max = 100) String newPassword) {}

    /* ===================== Admin ===================== */

    public record ZoneAdminDto(Long id, String code, String nameVi, String nameEn, String nameKo, boolean active, long poiCount) {}

    public record ZoneRequest(
            @NotBlank @Pattern(regexp = "[A-Z0-9_]{2,50}", message = "Mã chỉ gồm chữ IN HOA, số, dấu _") String code,
            @NotBlank @Size(max = 100) String nameVi,
            @Size(max = 100) String nameEn,
            @Size(max = 100) String nameKo,
            boolean active) {}

    public record PoiAdminDto(
            Long id, Long zoneId, String zoneCode, String code,
            BigDecimal latitude, BigDecimal longitude, int radiusM, String status,
            String titleVi, List<String> publishedLanguages, List<String> needsUpdate) {}

    public record PoiRequest(
            @NotNull Long zoneId,
            @NotBlank @Pattern(regexp = "[A-Z0-9_]{2,50}", message = "Mã chỉ gồm chữ IN HOA, số, dấu _") String code,
            @NotNull @DecimalMin("10.755") @DecimalMax("10.780") BigDecimal latitude,
            @NotNull @DecimalMin("106.625") @DecimalMax("106.650") BigDecimal longitude,
            @Min(5) @Max(300) int radiusM,
            @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") String status) {}

    public record AudioDto(Long id, String storageKey, String status, String source,
                           boolean matchesText, LocalDateTime createdAt, String previewUrl) {}

    public record VersionDto(
            Long id, String languageCode, int versionNo, String status, Long sourceVersionId,
            String title, String body, String contentHash, LocalDateTime createdAt,
            boolean published, List<AudioDto> audios) {}

    public record PublicationDto(String languageCode, Long versionId, Long audioAssetId,
                                 LocalDateTime publishedAt, boolean needsUpdate) {}

    public record LanguageContentDto(String languageCode, String languageName,
                                     PublicationDto publication, List<VersionDto> versions) {}

    public record PoiContentDto(PoiAdminDto poi, List<LanguageContentDto> languages) {}

    public record DraftRequest(
            @NotBlank @Pattern(regexp = "vi|en|ko") String languageCode,
            @NotBlank @Size(max = 255) String title,
            @NotBlank String body) {}

    public record DraftUpdateRequest(@NotBlank @Size(max = 255) String title, @NotBlank String body) {}

    public record QaIssue(String level, String message) {}

    public record QaResult(boolean canApprove, List<QaIssue> issues, String sourceTitle, String sourceBody) {}

    public record PublishRequest(
            @NotBlank @Pattern(regexp = "vi|en|ko") String languageCode,
            @NotNull Long versionId,
            Long audioAssetId) {}

    public record JobDto(
            Long id, String jobType, String status, Long targetVersionId, String targetLanguage,
            Long resultVersionId, Long poiId, String poiCode, String versionLanguage,
            LocalDateTime createdAt, LocalDateTime startedAt, LocalDateTime finishedAt,
            String errorMessage, int retryCount) {}

    public record TranslateRequest(@NotBlank @Pattern(regexp = "en|ko") String targetLanguage) {}

    public record GlossaryDto(
            Long id,
            @NotBlank @Size(max = 255) String term,
            @NotBlank @Pattern(regexp = "PLACE|RIDE|ZONE|PERSON|ANIMAL|ORGANIZATION|OTHER") String termType,
            boolean keepOriginal,
            @Size(max = 255) String translationEn,
            @Size(max = 255) String translationKo,
            @Size(max = 255) String ttsAliasVi,
            @Size(max = 255) String ttsAliasEn,
            @Size(max = 255) String ttsAliasKo,
            @Size(max = 500) String note) {}

    public record OverviewDto(long poiCount, long activePoiCount, long zoneCount,
                              List<LangCount> publishedByLanguage, List<NeedsUpdateItem> needsUpdate,
                              List<LangCount> jobsByStatus, boolean machineTranslationEnabled) {}

    public record LangCount(String key, long count) {}

    public record NeedsUpdateItem(Long poiId, String poiCode, String languageCode) {}
}
