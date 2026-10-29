package vn.edu.sgu.damsenguide.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class LocationCheckDto {

    public record NearbySuggestionRequest(
        @NotNull(message = "Vĩ độ không được để trống")
        @DecimalMin(value = "-90.0", message = "Vĩ độ phải >= -90")
        @DecimalMax(value = "90.0", message = "Vĩ độ phải <= 90")
        Double latitude,

        @NotNull(message = "Kinh độ không được để trống")
        @DecimalMin(value = "-180.0", message = "Kinh độ phải >= -180")
        @DecimalMax(value = "180.0", message = "Kinh độ phải <= 180")
        Double longitude,

        @Positive(message = "Sai số định vị (accuracyM) phải > 0")
        Double accuracyM,

        @PositiveOrZero(message = "Tuổi vị trí (locationAgeMs) phải >= 0")
        Long locationAgeMs,

        @NotBlank(message = "Ngôn ngữ không được để trống")
        @Pattern(regexp = "^(vi|en|ko)$", message = "UNSUPPORTED_LANGUAGE")
        String language,

        @NotBlank(message = "Phiên bản dữ liệu không được để trống")
        String packageVersion
    ) {}

    public record EligiblePoiItem(
        Long poiId,
        String code,
        String title,
        String servedLanguage,
        boolean fallback,
        long distanceM,
        int nearRadiusM,
        Long audioAssetId
    ) {}

    public record NearbySuggestionResponse(
        String packageVersion,
        String locationQuality,   // GOOD, FAIR, POOR, STALE
        boolean outOfArea,
        EligiblePoiItem suggestedPoi,       // Điểm ưu tiên nhất (hoặc null nếu rỗng)
        List<EligiblePoiItem> eligiblePois  // Toàn bộ điểm thỏa bán kính nhận diện (rỗng nếu không có)
    ) {}
}
