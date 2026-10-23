package com.example.multilingualnarration.integration.mock;

import com.example.multilingualnarration.integration.TextToSpeechProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * BẢN GIẢ LẬP (MOCK) — KHÔNG tạo file âm thanh thật.
 *
 * <p>Chỉ hoạt động với profile {@code mock-integrations}. Trả về URL dạng {@code mock://...}
 * (không phải http) để thể hiện rõ đây là dữ liệu giả lập, không phát được.
 */
@Component
@Profile("mock-integrations")
public class MockTextToSpeechProvider implements TextToSpeechProvider {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public String synthesize(String text, String languageCode) {
        return "mock://tts/" + languageCode + "/" + Integer.toHexString(text.hashCode()) + ".mp3";
    }
}
