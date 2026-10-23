package com.example.multilingualnarration.dto.response;

import com.example.multilingualnarration.enums.AudioStatus;
import com.example.multilingualnarration.enums.NarrationStatus;

/**
 * Một ngôn ngữ đã có bản thuyết minh cho địa điểm
 * ({@code GET /api/v1/locations/{locationId}/narrations/available-languages}).
 * Kèm narrationId + trạng thái để màn hình quản trị và module dịch/TTS biết cần xử lý gì.
 */
public record AvailableLanguageResponse(
        String languageCode,
        String languageName,
        String nativeName,
        Long narrationId,
        NarrationStatus status,
        AudioStatus audioStatus
) {
}
