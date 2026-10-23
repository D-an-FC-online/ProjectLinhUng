package com.example.multilingualnarration.service.impl;

import com.example.multilingualnarration.dto.request.CreateLocationRequest;
import com.example.multilingualnarration.dto.request.UpdateLocationRequest;
import com.example.multilingualnarration.dto.response.LocationResponse;
import com.example.multilingualnarration.dto.response.PageResponse;
import com.example.multilingualnarration.entity.Location;
import com.example.multilingualnarration.exception.BusinessException;
import com.example.multilingualnarration.exception.ErrorCode;
import com.example.multilingualnarration.exception.ResourceNotFoundException;
import com.example.multilingualnarration.mapper.LocationMapper;
import com.example.multilingualnarration.repository.LocationRepository;
import com.example.multilingualnarration.repository.NarrationRepository;
import com.example.multilingualnarration.service.LocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;

/**
 * Cài đặt nghiệp vụ địa điểm.
 *
 * <p>{@code @Service}: đánh dấu đây là Bean thuộc tầng nghiệp vụ, Spring tự tạo 1 instance
 * (singleton) và tiêm vào LocationController.
 *
 * <p>{@code @Transactional(readOnly = true)} ở mức class: mặc định mọi method chỉ đọc;
 * các method ghi dữ liệu khai báo lại {@code @Transactional} để mở transaction ghi.
 */
@Service
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    /** Chỉ cho phép sắp xếp theo các trường này (tránh lỗi khi client truyền tên trường lạ). */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "createdAt", "updatedAt");

    private final LocationRepository locationRepository;
    private final NarrationRepository narrationRepository;
    private final LocationMapper locationMapper;

    /**
     * Constructor injection: Spring thấy class chỉ có 1 constructor nên tự dùng nó, tìm các Bean
     * phù hợp kiểu tham số trong Container và truyền vào. Không cần {@code @Autowired}.
     * Các field là {@code final} => bắt buộc phải có dependency, không thể bị null.
     */
    public LocationServiceImpl(LocationRepository locationRepository,
                               NarrationRepository narrationRepository,
                               LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.narrationRepository = narrationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    @Transactional
    public LocationResponse create(CreateLocationRequest request) {
        validateCoordinates(request.latitude(), request.longitude());
        Location saved = locationRepository.save(locationMapper.toEntity(request));
        return locationMapper.toResponse(saved);
    }

    @Override
    public PageResponse<LocationResponse> getAll(int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<LocationResponse> result = locationRepository.findAll(pageable).map(locationMapper::toResponse);
        return PageResponse.from(result);
    }

    @Override
    public LocationResponse getById(Long id) {
        return locationMapper.toResponse(findLocationOrThrow(id));
    }

    @Override
    public PageResponse<LocationResponse> search(String keyword, int page, int size, String sortBy, String direction) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<LocationResponse> result = locationRepository
                .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                        normalizedKeyword, normalizedKeyword, pageable)
                .map(locationMapper::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional
    public LocationResponse update(Long id, UpdateLocationRequest request) {
        Location location = findLocationOrThrow(id);
        validateCoordinates(request.latitude(), request.longitude());
        locationMapper.updateEntity(location, request);
        // Không cần gọi save(): entity đang được Hibernate quản lý (managed), khi transaction
        // commit Hibernate tự phát hiện thay đổi (dirty checking) và sinh câu UPDATE.
        // Gọi saveAndFlush để updatedAt (@PreUpdate) có giá trị mới ngay trong response.
        Location saved = locationRepository.saveAndFlush(location);
        return locationMapper.toResponse(saved);
    }

    /**
     * Quy tắc nghiệp vụ khi xóa: KHÔNG cho xóa địa điểm đang có bản thuyết minh,
     * tránh mất nội dung đã biên soạn/dịch (tốn công và chi phí).
     * Muốn xóa phải xóa các bản thuyết minh trước.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw ResourceNotFoundException.location(id);
        }
        if (narrationRepository.existsByLocationId(id)) {
            throw new BusinessException(ErrorCode.LOCATION_HAS_NARRATIONS,
                    "Không thể xóa địa điểm id = " + id + " vì vẫn còn bản thuyết minh. Hãy xóa các bản thuyết minh trước");
        }
        locationRepository.deleteById(id);
    }

    // ------------------------------------------------------------------

    private Location findLocationOrThrow(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.location(id));
    }

    /** Vĩ độ và kinh độ phải đi theo cặp: có cả hai hoặc không có cái nào. */
    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BusinessException(ErrorCode.INVALID_COORDINATES,
                    "Vĩ độ (latitude) và kinh độ (longitude) phải được cung cấp cùng nhau");
        }
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException(ErrorCode.INVALID_SORT,
                    "Không hỗ trợ sắp xếp theo '" + sortBy + "'. Cho phép: " + ALLOWED_SORT_FIELDS);
        }
        Sort.Direction sortDirection = switch (direction == null ? "" : direction.toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new BusinessException(ErrorCode.INVALID_SORT,
                    "Chiều sắp xếp chỉ nhận 'asc' hoặc 'desc'");
        };
        Sort sort = Sort.by(sortDirection, sortBy);
        if (!"id".equals(sortBy)) {
            // Sắp xếp phụ theo id để thứ tự ổn định khi nhiều bản ghi trùng giá trị sắp xếp
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }
        return PageRequest.of(page, size, sort);
    }
}
