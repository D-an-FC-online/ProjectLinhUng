package com.example.multilingualnarration.mapper;

import com.example.multilingualnarration.dto.request.CreateLocationRequest;
import com.example.multilingualnarration.dto.request.UpdateLocationRequest;
import com.example.multilingualnarration.dto.response.LocationResponse;
import com.example.multilingualnarration.entity.Location;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Chuyển đổi qua lại giữa DTO và Entity {@link Location}.
 *
 * <p>Mapper viết tay (không dùng MapStruct) để dễ đọc, dễ debug và dễ giải thích.
 * Là Bean ({@code @Component}) nên được tiêm vào Service qua constructor.
 */
@Component
public class LocationMapper {

    /** Số chữ số thập phân của cột latitude/longitude: DECIMAL(9,6). */
    private static final int COORDINATE_SCALE = 6;

    public Location toEntity(CreateLocationRequest request) {
        Location location = new Location();
        location.setName(trim(request.name()));
        location.setDescription(trimToNull(request.description()));
        location.setAddress(trimToNull(request.address()));
        location.setLatitude(normalizeCoordinate(request.latitude()));
        location.setLongitude(normalizeCoordinate(request.longitude()));
        location.setImageUrl(trimToNull(request.imageUrl()));
        return location;
    }

    /** Ghi đè toàn bộ dữ liệu của entity bằng dữ liệu mới (ngữ nghĩa PUT). */
    public void updateEntity(Location location, UpdateLocationRequest request) {
        location.setName(trim(request.name()));
        location.setDescription(trimToNull(request.description()));
        location.setAddress(trimToNull(request.address()));
        location.setLatitude(normalizeCoordinate(request.latitude()));
        location.setLongitude(normalizeCoordinate(request.longitude()));
        location.setImageUrl(trimToNull(request.imageUrl()));
    }

    public LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getDescription(),
                location.getAddress(),
                location.getLatitude(),
                location.getLongitude(),
                location.getImageUrl(),
                DateTimeMapper.toOffsetDateTime(location.getCreatedAt()),
                DateTimeMapper.toOffsetDateTime(location.getUpdatedAt()));
    }

    /** Làm tròn về 6 chữ số thập phân cho khớp với kiểu DECIMAL(9,6) trong database. */
    private static BigDecimal normalizeCoordinate(BigDecimal value) {
        return value == null ? null : value.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /** Chuỗi rỗng/chỉ có khoảng trắng được lưu là NULL để dữ liệu nhất quán. */
    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
