package com.example.multilingualnarration.integration.mock;

import com.example.multilingualnarration.integration.TranslationProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * BẢN GIẢ LẬP (MOCK) — KHÔNG phải AI dịch thật.
 *
 * <p>Chỉ được tạo khi chạy với profile {@code mock-integrations}
 * (ví dụ SPRING_PROFILES_ACTIVE=dev,mock-integrations) để demo luồng dịch khi module AI chưa xong.
 * Kết quả KHÔNG dịch gì cả, chỉ gắn nhãn "[MOCK vi→ja]" vào đầu văn bản gốc để ai nhìn cũng biết
 * đây là dữ liệu giả lập. Bản thuyết minh tạo ra luôn ở trạng thái DRAFT nên khách không thấy.
 *
 * <p>Khi đã có provider thật, KHÔNG bật profile này (tránh có 2 Bean TranslationProvider).
 */
@Component
@Profile("mock-integrations")
public class MockTranslationProvider implements TranslationProvider {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public String translate(String text, String sourceLanguageCode, String targetLanguageCode) {
        return "[MOCK " + sourceLanguageCode + "→" + targetLanguageCode + "] " + text;
    }
}
