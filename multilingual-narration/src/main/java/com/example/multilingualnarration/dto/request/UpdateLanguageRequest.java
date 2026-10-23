package com.example.multilingualnarration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu cập nhật ngôn ngữ ({@code PUT /api/v1/languages/{code}}).
 * Mã ngôn ngữ (code) KHÔNG được đổi vì đang được tham chiếu bởi Frontend và các bản thuyết minh.
 */
@Schema(description = "Thông tin cập nhật ngôn ngữ (không đổi được mã)")
public record UpdateLanguageRequest(

        @Schema(example = "Tiếng Pháp")
        @NotBlank(message = "Tên ngôn ngữ không được để trống")
        @Size(max = 100, message = "Tên ngôn ngữ tối đa 100 ký tự")
        String name,

        @Schema(example = "Français")
        @Size(max = 100, message = "Tên bản ngữ tối đa 100 ký tự")
        String nativeName,

        @Schema(example = "false", description = "false = tạm ngừng hỗ trợ ngôn ngữ này")
        @NotNull(message = "Trạng thái kích hoạt (active) là bắt buộc")
        Boolean active
) {
}
