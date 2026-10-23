package com.example.multilingualnarration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dữ liệu client gửi lên khi tạo địa điểm ({@code POST /api/v1/locations}).
 *
 * <p>Dùng Java record: bất biến, tự sinh constructor/getter/equals/hashCode/toString => không cần Lombok.
 * Các annotation validation được kiểm tra tự động khi Controller khai báo {@code @Valid}.
 */
@Schema(description = "Thông tin tạo địa điểm tham quan")
public record CreateLocationRequest(

        @Schema(example = "Văn Miếu – Quốc Tử Giám")
        @NotBlank(message = "Tên địa điểm không được để trống")
        @Size(max = 255, message = "Tên địa điểm tối đa 255 ký tự")
        String name,

        @Schema(example = "Quần thể di tích thờ Khổng Tử và trường đại học đầu tiên của Việt Nam.")
        @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
        String description,

        @Schema(example = "58 Quốc Tử Giám, Hà Nội")
        @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
        String address,

        @Schema(example = "21.027800")
        @DecimalMin(value = "-90.0", message = "Vĩ độ phải nằm trong khoảng -90 đến 90")
        @DecimalMax(value = "90.0", message = "Vĩ độ phải nằm trong khoảng -90 đến 90")
        BigDecimal latitude,

        @Schema(example = "105.835600")
        @DecimalMin(value = "-180.0", message = "Kinh độ phải nằm trong khoảng -180 đến 180")
        @DecimalMax(value = "180.0", message = "Kinh độ phải nằm trong khoảng -180 đến 180")
        BigDecimal longitude,

        @Schema(example = "https://example.com/images/van-mieu.jpg")
        @Size(max = 1000, message = "URL ảnh tối đa 1000 ký tự")
        @Pattern(regexp = "^https?://.+", message = "URL ảnh phải bắt đầu bằng http:// hoặc https://")
        String imageUrl
) {
}
