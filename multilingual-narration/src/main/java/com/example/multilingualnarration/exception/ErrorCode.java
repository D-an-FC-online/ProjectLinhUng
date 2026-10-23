package com.example.multilingualnarration.exception;

import org.springframework.http.HttpStatus;

/**
 * Danh mục mã lỗi của hệ thống. Mỗi mã gắn sẵn HTTP status tương ứng.
 *
 * <p>Frontend và các module khác nên rẽ nhánh theo {@code code} (ổn định),
 * không nên so sánh chuỗi {@code message} (có thể đổi câu chữ).
 */
public enum ErrorCode {

    // ----- 400 Bad Request: dữ liệu gửi lên sai -----
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST),
    INVALID_COORDINATES(HttpStatus.BAD_REQUEST),
    INVALID_SORT(HttpStatus.BAD_REQUEST),
    LANGUAGE_INACTIVE(HttpStatus.BAD_REQUEST),
    SAME_LANGUAGE(HttpStatus.BAD_REQUEST),

    // ----- 404 Not Found -----
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND),
    LANGUAGE_NOT_FOUND(HttpStatus.NOT_FOUND),
    NARRATION_NOT_FOUND(HttpStatus.NOT_FOUND),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND),

    // ----- 405 / 415 -----
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // ----- 409 Conflict: vi phạm quy tắc nghiệp vụ / trùng dữ liệu -----
    LANGUAGE_ALREADY_EXISTS(HttpStatus.CONFLICT),
    NARRATION_ALREADY_EXISTS(HttpStatus.CONFLICT),
    LOCATION_HAS_NARRATIONS(HttpStatus.CONFLICT),
    NARRATION_PUBLISHED(HttpStatus.CONFLICT),
    DATA_CONFLICT(HttpStatus.CONFLICT),

    // ----- 502 / 503: dịch vụ tích hợp (AI dịch thuật, TTS) -----
    TRANSLATION_FAILED(HttpStatus.BAD_GATEWAY),
    TTS_FAILED(HttpStatus.BAD_GATEWAY),
    TRANSLATION_PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    TTS_PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),

    // ----- 500 -----
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
