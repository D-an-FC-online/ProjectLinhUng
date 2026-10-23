package com.example.multilingualnarration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dữ liệu cập nhật địa điểm ({@code PUT /api/v1/locations/{id}}).
 *
 * <p>PUT = THAY THẾ TOÀN BỘ: trường nào gửi null thì giá trị trong database cũng thành null
 * (trừ {@code name} là bắt buộc). Frontend nên gửi đầy đủ các trường đang có.
 *
 * <p>Tách riêng với {@link CreateLocationRequest} (dù hiện giống nhau) để sau này 2 thao tác
 * có thể có quy tắc khác nhau mà không ảnh hưởng lẫn nhau.
 */
@Schema(description = "Thông tin cập nhật địa điểm (thay thế toàn bộ)")
public record UpdateLocationRequest(

        @Schema(example = "Văn Miếu – Quốc Tử Giám")
        @NotBlank(message = "Tên địa điểm không được để trống")
        @Size(max = 255, message = "Tên địa điểm tối đa 255 ký tự")
        String name,

        @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
        String description,

        @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
        String address,

        @DecimalMin(value = "-90.0", message = "Vĩ độ phải nằm trong khoảng -90 đến 90")
        @DecimalMax(value = "90.0", message = "Vĩ độ phải nằm trong khoảng -90 đến 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Kinh độ phải nằm trong khoảng -180 đến 180")
        @DecimalMax(value = "180.0", message = "Kinh độ phải nằm trong khoảng -180 đến 180")
        BigDecimal longitude,

        @Size(max = 1000, message = "URL ảnh tối đa 1000 ký tự")
        @Pattern(regexp = "^https?://.+", message = "URL ảnh phải bắt đầu bằng http:// hoặc https://")
        String imageUrl
) {
}
