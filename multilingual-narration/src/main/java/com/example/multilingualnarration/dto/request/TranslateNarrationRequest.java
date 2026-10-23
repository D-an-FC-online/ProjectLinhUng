package com.example.multilingualnarration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yêu cầu dịch một bản thuyết minh sang ngôn ngữ khác
 * ({@code POST /api/v1/narrations/{id}/translations}).
 */
@Schema(description = "Ngôn ngữ đích cần dịch sang")
public record TranslateNarrationRequest(

        @Schema(example = "ko")
        @NotBlank(message = "Mã ngôn ngữ đích không được để trống")
        @Size(max = 10, message = "Mã ngôn ngữ tối đa 10 ký tự")
        String targetLanguageCode
) {
}
