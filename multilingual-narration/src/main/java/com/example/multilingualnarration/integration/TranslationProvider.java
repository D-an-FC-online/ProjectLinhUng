package com.example.multilingualnarration.integration;

/**
 * RANH GIỚI TÍCH HỢP với module DỊCH THUẬT TỰ ĐỘNG (AI) — do thành viên phụ trách AI cài đặt.
 *
 * <p>Core Backend chỉ định nghĩa "hợp đồng" này và KHÔNG giả định đã có dịch vụ AI thật.
 * Cách cắm module dịch vào hệ thống:
 * <pre>
 * &#64;Component
 * public class GeminiTranslationProvider implements TranslationProvider {
 *     public String getName() { return "gemini"; }
 *     public String translate(String text, String source, String target) {
 *         // gọi API AI thật ở đây (đọc API key từ biến môi trường, KHÔNG hardcode)
 *     }
 * }
 * </pre>
 * Spring tự phát hiện Bean mới và tiêm vào NarrationServiceImpl, không phải sửa code Core.
 * Nếu chưa có Bean nào cài đặt interface này, API dịch trả về 503 TRANSLATION_PROVIDER_UNAVAILABLE.
 *
 * <p>Lưu ý: chỉ được có MỘT Bean cài đặt interface này tại một thời điểm.
 */
public interface TranslationProvider {

    /** Tên provider để ghi log, ví dụ "gemini", "google-translate", "mock". */
    String getName();

    /**
     * Dịch văn bản.
     *
     * @param text               văn bản gốc
     * @param sourceLanguageCode mã ngôn ngữ nguồn, ví dụ "vi"
     * @param targetLanguageCode mã ngôn ngữ đích, ví dụ "ja"
     * @return văn bản đã dịch
     * @throws RuntimeException nếu dịch thất bại (Core sẽ chuyển thành lỗi 502 TRANSLATION_FAILED)
     */
    String translate(String text, String sourceLanguageCode, String targetLanguageCode);
}
