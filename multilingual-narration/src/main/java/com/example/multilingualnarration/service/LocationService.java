package com.example.multilingualnarration.service;

import com.example.multilingualnarration.dto.request.CreateLocationRequest;
import com.example.multilingualnarration.dto.request.UpdateLocationRequest;
import com.example.multilingualnarration.dto.response.LocationResponse;
import com.example.multilingualnarration.dto.response.PageResponse;

/**
 * Business Logic Layer — nghiệp vụ quản lý địa điểm.
 *
 * <p>Controller chỉ phụ thuộc vào interface này (không phụ thuộc class cài đặt cụ thể).
 * Nhận vào / trả ra DTO, không để Entity rò rỉ lên tầng Presentation.
 */
public interface LocationService {

    LocationResponse create(CreateLocationRequest request);

    PageResponse<LocationResponse> getAll(int page, int size, String sortBy, String direction);

    LocationResponse getById(Long id);

    PageResponse<LocationResponse> search(String keyword, int page, int size, String sortBy, String direction);

    LocationResponse update(Long id, UpdateLocationRequest request);

    void delete(Long id);
}
