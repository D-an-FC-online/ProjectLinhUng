package vn.edu.sgu.damsen.provider;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import vn.edu.sgu.damsen.config.AppProperties;

/**
 * Gọi Google Cloud Translation v2 và Text-to-Speech v1 qua REST với API key (biến môi trường GOOGLE_API_KEY).
 * Không có key thì isEnabled() = false và job báo lỗi rõ ràng thay vì treo.
 */
@Component
public class GoogleCloudProviders implements TranslationProvider, TtsProvider {
    private static final String NOT_CONFIGURED =
            "Chưa cấu hình GOOGLE_API_KEY nên hệ thống chưa gọi được dịch vụ %s. "
            + "Bạn có thể nhập %s thủ công trong trang quản trị.";

    private final AppProperties.Google cfg;
    private final RestClient http = RestClient.create();

    public GoogleCloudProviders(AppProperties props) {
        this.cfg = props.google();
    }

    @Override
    public boolean isEnabled() {
        return cfg != null && cfg.enabled();
    }

    @Override
    public String translateHtml(String html, String sourceLang, String targetLang) {
        if (!isEnabled()) throw new ProviderException(NOT_CONFIGURED.formatted("dịch máy", "bản dịch"));
        try {
            JsonNode res = http.post()
                    .uri(cfg.translateUrl() + "?key={k}", cfg.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("q", List.of(html), "source", sourceLang, "target", targetLang, "format", "html"))
                    .retrieve().body(JsonNode.class);
            JsonNode t = res == null ? null : res.at("/data/translations/0/translatedText");
            if (t == null || t.isMissingNode()) throw new ProviderException("Dịch vụ dịch trả về dữ liệu rỗng");
            return t.asText();
        } catch (RestClientResponseException e) {
            throw new ProviderException("Dịch vụ dịch báo lỗi " + e.getStatusCode().value() + ": "
                    + e.getResponseBodyAsString(), e);
        }
    }

    @Override
    public byte[] synthesizeSsml(String ssml, String lang) {
        if (!isEnabled()) throw new ProviderException(NOT_CONFIGURED.formatted("tạo giọng đọc (TTS)", "file âm thanh bằng cách tải lên"));
        String voice = cfg.voices() == null ? null : cfg.voices().get(lang);
        if (voice == null) throw new ProviderException("Chưa cấu hình giọng đọc cho ngôn ngữ " + lang);
        String languageCode = voice.substring(0, voice.indexOf('-', voice.indexOf('-') + 1)); // vd: vi-VN
        try {
            JsonNode res = http.post()
                    .uri(cfg.ttsUrl() + "?key={k}", cfg.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "input", Map.of("ssml", ssml),
                            "voice", Map.of("languageCode", languageCode, "name", voice),
                            "audioConfig", Map.of("audioEncoding", "MP3")))
                    .retrieve().body(JsonNode.class);
            String b64 = res == null ? null : res.path("audioContent").asText(null);
            if (b64 == null || b64.isEmpty()) throw new ProviderException("Dịch vụ TTS trả về dữ liệu rỗng");
            return Base64.getDecoder().decode(b64);
        } catch (RestClientResponseException e) {
            throw new ProviderException("Dịch vụ TTS báo lỗi " + e.getStatusCode().value() + ": "
                    + e.getResponseBodyAsString(), e);
        }
    }
}
