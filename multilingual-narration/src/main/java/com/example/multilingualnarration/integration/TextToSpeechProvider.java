package com.example.multilingualnarration.integration;

/**
 * RANH GIỚI TÍCH HỢP với module TEXT-TO-SPEECH (TTS) — do thành viên phụ trách TTS cài đặt.
 *
 * <p>Provider chịu trách nhiệm: gọi dịch vụ TTS, LƯU file âm thanh (ổ đĩa, S3, Cloudinary...)
 * và trả về URL công khai của file. Core Backend chỉ lưu URL vào cột {@code narrations.audio_url}.
 *
 * <p>Cách cắm vào giống {@link TranslationProvider}: tạo class {@code @Component} cài đặt interface.
 * Nếu chưa có, API tạo audio trả về 503 TTS_PROVIDER_UNAVAILABLE.
 *
 * <p>Nếu module TTS là một dịch vụ RIÊNG (ví dụ viết bằng Python), không cần interface này:
 * dịch vụ đó gọi {@code PUT /api/v1/narrations/{id}/audio} để gắn URL sau khi tạo xong file.
 */
public interface TextToSpeechProvider {

    /** Tên provider để ghi log. */
    String getName();

    /**
     * Chuyển văn bản thành giọng nói.
     *
     * @param text         nội dung cần đọc
     * @param languageCode mã ngôn ngữ của văn bản, ví dụ "en"
     * @return URL công khai của file âm thanh đã lưu
     * @throws RuntimeException nếu thất bại (Core sẽ chuyển thành lỗi 502 TTS_FAILED)
     */
    String synthesize(String text, String languageCode);
}
