package com.example.multilingualnarration.dto.response;

import com.example.multilingualnarration.enums.AudioStatus;
import com.example.multilingualnarration.enums.NarrationAvailability;

import java.util.List;

/**
 * Kết quả tra cứu thuyết minh theo địa điểm + ngôn ngữ
 * ({@code GET /api/v1/locations/{locationId}/narrations?language=xx}).
 *
 * <p>Vì địa điểm và ngôn ngữ đều hợp lệ, câu hỏi "đã có nội dung chưa?" luôn có câu trả lời
 * => trả 200 kèm {@code availability} rõ ràng, thay vì 404. Frontend chỉ cần đọc availability:
 * <ul>
 *   <li>AVAILABLE: hiển thị {@code narration}.</li>
 *   <li>PENDING: báo "nội dung đang được chuẩn bị".</li>
 *   <li>NOT_AVAILABLE: gợi ý ngôn ngữ khác trong {@code availableLanguages}.</li>
 * </ul>
 *
 * @param narration          chỉ có giá trị khi availability = AVAILABLE, ngược lại là null
 * @param availableLanguages mã các ngôn ngữ ĐÃ CÓ bản PUBLISHED cho địa điểm này
 */
public record NarrationLookupResponse(
        Long locationId,
        String locationName,
        String requestedLanguage,
        NarrationAvailability availability,
        AudioStatus audioStatus,
        String message,
        NarrationResponse narration,
        List<String> availableLanguages
) {
}
