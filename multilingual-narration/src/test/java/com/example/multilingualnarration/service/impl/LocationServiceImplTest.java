package com.example.multilingualnarration.service.impl;

import com.example.multilingualnarration.dto.request.CreateLocationRequest;
import com.example.multilingualnarration.dto.response.LocationResponse;
import com.example.multilingualnarration.dto.response.PageResponse;
import com.example.multilingualnarration.entity.Location;
import com.example.multilingualnarration.exception.BusinessException;
import com.example.multilingualnarration.exception.ErrorCode;
import com.example.multilingualnarration.exception.ResourceNotFoundException;
import com.example.multilingualnarration.mapper.LocationMapper;
import com.example.multilingualnarration.repository.LocationRepository;
import com.example.multilingualnarration.repository.NarrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho tầng Service: KHÔNG khởi động Spring, KHÔNG cần database.
 * Repository được thay bằng mock (Mockito) => test chạy rất nhanh và chỉ kiểm tra nghiệp vụ.
 *
 * <p>Nhờ constructor injection, ta tự tạo Service bằng "new" và truyền mock vào — đây chính là
 * lợi ích lớn của Dependency Injection khi viết test.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private NarrationRepository narrationRepository;

    private LocationServiceImpl locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(locationRepository, narrationRepository, new LocationMapper());
    }

    @Test
    @DisplayName("Tạo địa điểm thành công: tên được trim, tọa độ làm tròn 6 chữ số thập phân")
    void create_success() {
        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location toSave = invocation.getArgument(0);
            toSave.setId(10L);          // giả lập database sinh id
            return toSave;
        });
        CreateLocationRequest request = new CreateLocationRequest(
                "  Dinh Độc Lập  ", "Mô tả", "135 Nam Kỳ Khởi Nghĩa",
                new BigDecimal("10.7770001"), new BigDecimal("106.6953"), null);

        LocationResponse response = locationService.create(request);

        assertEquals(10L, response.id());
        assertEquals("Dinh Độc Lập", response.name());
        assertEquals(new BigDecimal("10.777000"), response.latitude());
        assertEquals(new BigDecimal("106.695300"), response.longitude());

        ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
        verify(locationRepository).save(captor.capture());
        assertEquals("Dinh Độc Lập", captor.getValue().getName());
    }

    @Test
    @DisplayName("Tạo địa điểm chỉ có vĩ độ mà thiếu kinh độ => INVALID_COORDINATES, không lưu")
    void create_onlyLatitude_throwsInvalidCoordinates() {
        CreateLocationRequest request = new CreateLocationRequest(
                "Chợ Bến Thành", null, null, new BigDecimal("10.77"), null, null);

        BusinessException ex = assertThrows(BusinessException.class, () -> locationService.create(request));

        assertEquals(ErrorCode.INVALID_COORDINATES, ex.getErrorCode());
        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lấy địa điểm không tồn tại => ResourceNotFoundException (LOCATION_NOT_FOUND)")
    void getById_notFound() {
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> locationService.getById(999L));

        assertEquals(ErrorCode.LOCATION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("Tìm kiếm: từ khóa được trim, áp dụng phân trang và sắp xếp")
    void search_trimsKeywordAndAppliesPaging() {
        Location location = location(1L, "Văn Miếu – Quốc Tử Giám");
        when(locationRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                eq("văn miếu"), eq("văn miếu"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(location), PageRequest.of(0, 5), 1));

        PageResponse<LocationResponse> result = locationService.search("  văn miếu ", 0, 5, "name", "asc");

        assertEquals(1, result.totalElements());
        assertEquals("Văn Miếu – Quốc Tử Giám", result.content().get(0).name());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(locationRepository).findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                eq("văn miếu"), eq("văn miếu"), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("name").getDirection());
    }

    @Test
    @DisplayName("Sắp xếp theo trường không cho phép => INVALID_SORT, không truy vấn database")
    void getAll_invalidSortField() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> locationService.getAll(0, 10, "password", "asc"));

        assertEquals(ErrorCode.INVALID_SORT, ex.getErrorCode());
        verify(locationRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Xóa địa điểm còn bản thuyết minh => LOCATION_HAS_NARRATIONS, không xóa")
    void delete_locationWithNarrations_isRejected() {
        when(locationRepository.existsById(1L)).thenReturn(true);
        when(narrationRepository.existsByLocationId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> locationService.delete(1L));

        assertEquals(ErrorCode.LOCATION_HAS_NARRATIONS, ex.getErrorCode());
        verify(locationRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Xóa địa điểm không có bản thuyết minh => xóa thành công")
    void delete_locationWithoutNarrations_success() {
        when(locationRepository.existsById(4L)).thenReturn(true);
        when(narrationRepository.existsByLocationId(4L)).thenReturn(false);

        locationService.delete(4L);

        verify(locationRepository).deleteById(4L);
    }

    private static Location location(Long id, String name) {
        Location location = new Location();
        location.setId(id);
        location.setName(name);
        return location;
    }
}
