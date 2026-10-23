package com.example.multilingualnarration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu thêm ngôn ngữ mới ({@code POST /api/v1/languages}).
 */
@Schema(description = "Thông tin thêm ngôn ngữ mới")
public record CreateLanguageRequest(

        @Schema(example = "fr", description = "Mã ISO 639-1 / BCP 47, ví dụ: fr, de, zh-tw")
        @NotBlank(message = "Mã ngôn ngữ không được để trống")
        @Pattern(regexp = "^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})?$",
                message = "Mã ngôn ngữ không hợp lệ (ví dụ hợp lệ: vi, en, ja, zh-tw)")
        String code,

        @Schema(example = "Tiếng Pháp")
        @NotBlank(message = "Tên ngôn ngữ không được để trống")
        @Size(max = 100, message = "Tên ngôn ngữ tối đa 100 ký tự")
        String name,

        @Schema(example = "Français")
        @Size(max = 100, message = "Tên bản ngữ tối đa 100 ký tự")
        String nativeName,

        @Schema(example = "true", description = "Mặc định true nếu không gửi")
        Boolean active
) {
}
