package com.example.multilingualnarration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Điểm khởi chạy ứng dụng.
 *
 * <p>{@code @SpringBootApplication} gộp 3 annotation:
 * <ul>
 *   <li>{@code @Configuration}: class này là nguồn cấu hình Bean.</li>
 *   <li>{@code @EnableAutoConfiguration}: Spring Boot tự cấu hình DataSource, JPA, Flyway,
 *       Jackson, Tomcat... dựa trên thư viện có trong classpath và file application.yml.</li>
 *   <li>{@code @ComponentScan}: quét package {@code com.example.multilingualnarration} và mọi
 *       package con để tìm {@code @RestController}, {@code @Service}, {@code @Component},
 *       {@code @Configuration}... rồi đăng ký chúng thành Bean trong Spring Container.</li>
 * </ul>
 * Vì vậy mọi class cần được Spring quản lý PHẢI nằm trong package này hoặc package con.
 */
@SpringBootApplication
public class MultilingualNarrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultilingualNarrationApplication.class, args);
    }
}
