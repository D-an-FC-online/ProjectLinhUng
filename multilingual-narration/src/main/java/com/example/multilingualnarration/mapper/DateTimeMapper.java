package com.example.multilingualnarration.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Chuyển thời gian lưu trong database (LocalDateTime, theo múi giờ của server) sang
 * OffsetDateTime khi trả ra API, để JSON luôn có múi giờ rõ ràng, ví dụ
 * "2026-10-22T20:15:30+07:00". Frontend nhờ đó hiển thị đúng giờ ở mọi nơi.
 */
final class DateTimeMapper {

    private DateTimeMapper() {
    }

    static OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
