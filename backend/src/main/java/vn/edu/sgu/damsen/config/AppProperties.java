package vn.edu.sgu.damsen.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Các cấu hình "app.*" trong application.yml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String mediaDir, Nearby nearby, Jobs jobs, Google google) {

    public record Nearby(int defaultRadiusM, int groupDistanceM) {}

    public record Jobs(long pollIntervalMs, int maxRetries) {}

    public record Google(String apiKey, String translateUrl, String ttsUrl, Map<String, String> voices) {
        public boolean enabled() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
