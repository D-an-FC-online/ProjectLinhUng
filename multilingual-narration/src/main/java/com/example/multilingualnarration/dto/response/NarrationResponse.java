package com.example.multilingualnarration.dto.response;

import com.example.multilingualnarration.enums.AudioStatus;
import com.example.multilingualnarration.enums.NarrationStatus;

import java.time.OffsetDateTime;

/**
 * Chi tiết một bản thuyết minh. Thông tin địa điểm và ngôn ngữ được "làm phẳng"
 * (locationId, locationName, languageCode...) thay vì lồng nguyên object Entity.
 */
public record NarrationResponse(
        Long id,
        Long locationId,
        String locationName,
        String languageCode,
        String languageName,
        String title,
        String content,
        String audioUrl,
        AudioStatus audioStatus,
        NarrationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
