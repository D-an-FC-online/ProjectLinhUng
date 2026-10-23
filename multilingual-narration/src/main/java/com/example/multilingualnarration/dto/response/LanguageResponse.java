package com.example.multilingualnarration.dto.response;

/**
 * Thông tin ngôn ngữ trả về cho client.
 * Client tham chiếu ngôn ngữ bằng {@code code} (vi, en...), không cần biết id trong database.
 */
public record LanguageResponse(
        String code,
        String name,
        String nativeName,
        boolean active
) {
}
