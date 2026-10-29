package vn.edu.sgu.damsenguide.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ManifestResponse(
    String packageVersion,
    List<String> supportedLanguages,
    List<PoiManifestItem> pois,
    List<AudioManifestItem> audioAssets
) {
    public record PoiManifestItem(
        Long poiId,
        String code,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer nearRadiusM,
        Integer sortOrder,
        Map<String, LocalizedContentDto> contents // key: "vi", "en", "ko"
    ) {}

    public record LocalizedContentDto(
        String title,
        String body,
        Long audioAssetId
    ) {}

    public record AudioManifestItem(
        Long audioAssetId,
        String url,
        String mimeType,
        Integer durationMs,
        Long fileSizeBytes,
        String checksumSha256
    ) {}
}
