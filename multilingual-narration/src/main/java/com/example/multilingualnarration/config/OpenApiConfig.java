package com.example.multilingualnarration.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình thông tin chung hiển thị trên Swagger UI.
 *
 * <p>Ví dụ về {@code @Configuration} + {@code @Bean}: Spring gọi method
 * {@link #multilingualNarrationOpenAPI()} đúng một lần khi khởi động, lấy object trả về
 * và quản lý nó như một Bean. Thư viện springdoc sẽ tự tìm Bean kiểu {@link OpenAPI} này.
 *
 * <ul>
 *   <li>Swagger UI: http://localhost:8080/swagger-ui.html</li>
 *   <li>OpenAPI JSON (import vào Postman): http://localhost:8080/v3/api-docs</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI multilingualNarrationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Multilingual Auto Narration System API")
                        .version("v1")
                        .description("""
                                Core Backend cho đề tài "Thuyết minh tự động đa ngôn ngữ".
                                Quản lý địa điểm tham quan, ngôn ngữ hỗ trợ và nội dung thuyết minh.

                                Quy ước chung:
                                - Prefix: /api/v1
                                - Lỗi luôn trả về cùng một định dạng ErrorResponse (code, message, path...)
                                - Phân trang: page bắt đầu từ 0, size tối đa 100
                                - Dữ liệu seed chỉ phục vụ demo, KHÔNG phải dữ liệu chính thức
                                """)
                        .license(new License().name("Đồ án sinh viên - mục đích học tập")));
    }
}
