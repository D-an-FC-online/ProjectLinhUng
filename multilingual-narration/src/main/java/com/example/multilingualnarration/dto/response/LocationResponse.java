package com.example.multilingualnarration.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Dữ liệu địa điểm trả về cho client. Chỉ chứa những gì client cần,
 * không chứa quan hệ JPA, không lộ cấu trúc bảng.
 */
public record LocationResponse(
        Long id,
        String name,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
