package com.example.multilingualnarration.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Định dạng chung cho MỌI response có phân trang.
 *
 * <p>Không trả thẳng object {@link Page} của Spring vì cấu trúc JSON của nó dài, phụ thuộc
 * phiên bản Spring và có thể thay đổi. Record này là hợp đồng ổn định với Frontend.
 *
 * @param page số trang hiện tại, bắt đầu từ 0
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
