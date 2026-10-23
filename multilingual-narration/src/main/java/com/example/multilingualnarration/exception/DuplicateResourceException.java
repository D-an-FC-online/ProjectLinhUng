package com.example.multilingualnarration.exception;

/**
 * Dữ liệu bị trùng với dữ liệu đã có (mã ngôn ngữ, bản thuyết minh cùng địa điểm + ngôn ngữ)
 * => HTTP 409 Conflict.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
