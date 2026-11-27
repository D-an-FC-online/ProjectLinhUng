package vn.edu.sgu.damsen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Điểm khởi động ứng dụng: Thuyết minh đa ngôn ngữ theo vị trí - Đầm Sen. */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class DamSenGuideApplication {
    public static void main(String[] args) {
        SpringApplication.run(DamSenGuideApplication.class, args);
    }
}
