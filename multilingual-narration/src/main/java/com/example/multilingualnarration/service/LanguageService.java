package com.example.multilingualnarration.service;

import com.example.multilingualnarration.dto.request.CreateLanguageRequest;
import com.example.multilingualnarration.dto.request.UpdateLanguageRequest;
import com.example.multilingualnarration.dto.response.LanguageResponse;

import java.util.List;

/**
 * Business Logic Layer — nghiệp vụ quản lý ngôn ngữ hỗ trợ.
 */
public interface LanguageService {

    /**
     * @param includeInactive false (mặc định) = chỉ ngôn ngữ đang kích hoạt (dùng cho khách);
     *                        true = tất cả (dùng cho màn hình quản trị)
     */
    List<LanguageResponse> getAll(boolean includeInactive);

    /** Trả về cả ngôn ngữ đã tắt; client đọc trường {@code active} để biết có được kích hoạt không. */
    LanguageResponse getByCode(String code);

    LanguageResponse create(CreateLanguageRequest request);

    LanguageResponse update(String code, UpdateLanguageRequest request);
}
