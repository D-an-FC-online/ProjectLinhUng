package com.example.multilingualnarration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Gắn URL file âm thanh cho bản thuyết minh ({@code PUT /api/v1/narrations/{id}/audio}).
 * Dành cho module TTS chạy như một dịch vụ riêng: tạo file xong thì gọi API này để báo kết quả.
 */
@Schema(description = "URL file âm thanh đã được tạo")
public record AttachAudioRequest(

        @Schema(example = "https://cdn.example.com/audio/van-mieu-en.mp3")
        @NotBlank(message = "URL âm thanh không được để trống")
        @Size(max = 1000, message = "URL âm thanh tối đa 1000 ký tự")
        @Pattern(regexp = "^https?://.+", message = "URL âm thanh phải bắt đầu bằng http:// hoặc https://")
        String audioUrl
) {
}
