package com.example.multilingualnarration.dto.request;

import com.example.multilingualnarration.enums.NarrationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu tạo bản thuyết minh cho một địa điểm
 * ({@code POST /api/v1/locations/{locationId}/narrations}).
 * locationId nằm trên URL nên không lặp lại trong body.
 */
@Schema(description = "Thông tin tạo bản thuyết minh")
public record CreateNarrationRequest(

        @Schema(example = "vi")
        @NotBlank(message = "Mã ngôn ngữ không được để trống")
        @Size(max = 10, message = "Mã ngôn ngữ tối đa 10 ký tự")
        String languageCode,

        @Schema(example = "Văn Miếu – Quốc Tử Giám: Trường đại học đầu tiên của Việt Nam")
        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
        String title,

        @Schema(example = "Văn Miếu được xây dựng năm 1070 dưới triều vua Lý Thánh Tông...")
        @NotBlank(message = "Nội dung thuyết minh không được để trống")
        @Size(max = 50000, message = "Nội dung tối đa 50000 ký tự")
        String content,

        @Schema(example = "https://cdn.example.com/audio/van-mieu-vi.mp3",
                description = "Không bắt buộc. Thường để trống và do module TTS cập nhật sau")
        @Size(max = 1000, message = "URL âm thanh tối đa 1000 ký tự")
        @Pattern(regexp = "^https?://.+", message = "URL âm thanh phải bắt đầu bằng http:// hoặc https://")
        String audioUrl,

        @Schema(example = "DRAFT", description = "Không bắt buộc, mặc định DRAFT")
        NarrationStatus status
) {
}
