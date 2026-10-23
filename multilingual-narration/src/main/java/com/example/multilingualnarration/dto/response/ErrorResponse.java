package com.example.multilingualnarration.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Định dạng lỗi THỐNG NHẤT cho toàn bộ API. Mọi lỗi (validation, không tìm thấy, trùng dữ liệu,
 * lỗi hệ thống...) đều trả về đúng cấu trúc này, Frontend chỉ cần viết 1 hàm xử lý lỗi.
 *
 * <pre>
 * {
 *   "timestamp": "2026-10-22T20:15:30.123+07:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "code": "LOCATION_NOT_FOUND",
 *   "message": "Không tìm thấy địa điểm với id = 999",
 *   "path": "/api/v1/locations/999"
 * }
 * </pre>
 *
 * @param code    mã lỗi ổn định cho máy đọc (Frontend dùng để rẽ nhánh), xem ErrorCode
 * @param message thông điệp cho người đọc (tiếng Việt), có thể thay đổi câu chữ
 * @param details danh sách lỗi theo từng trường (chỉ có với lỗi validation)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldErrorDetail> details
) {

    /** Lỗi của một trường cụ thể, ví dụ {"field": "name", "message": "Tên địa điểm không được để trống"}. */
    public record FieldErrorDetail(String field, String message) {
    }
}
