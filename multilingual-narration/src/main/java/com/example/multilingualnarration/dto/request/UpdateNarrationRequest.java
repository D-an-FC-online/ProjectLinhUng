package com.example.multilingualnarration.dto.request;

import com.example.multilingualnarration.enums.NarrationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu cập nhật bản thuyết minh ({@code PUT /api/v1/narrations/{id}}).
 *
 * <p>Không cho đổi địa điểm/ngôn ngữ (muốn đổi thì xóa và tạo bản mới) để không phá vỡ
 * quy tắc "mỗi địa điểm chỉ có 1 bản cho mỗi ngôn ngữ".
 *
 * <p>Không có audioUrl: âm thanh được quản lý riêng qua {@code PUT /api/v1/narrations/{id}/audio}.
 * Nếu nội dung thay đổi, audio cũ không còn khớp nên hệ thống tự xóa audioUrl.
 */
@Schema(description = "Thông tin cập nhật bản thuyết minh")
public record UpdateNarrationRequest(

        @Schema(example = "Văn Miếu – Quốc Tử Giám")
        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
        String title,

        @Schema(example = "Nội dung thuyết minh đã chỉnh sửa...")
        @NotBlank(message = "Nội dung thuyết minh không được để trống")
        @Size(max = 50000, message = "Nội dung tối đa 50000 ký tự")
        String content,

        @Schema(example = "PUBLISHED")
        @NotNull(message = "Trạng thái (status) là bắt buộc: DRAFT hoặc PUBLISHED")
        NarrationStatus status
) {
}
