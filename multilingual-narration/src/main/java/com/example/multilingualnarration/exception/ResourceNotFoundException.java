package com.example.multilingualnarration.exception;

/**
 * Không tìm thấy tài nguyên (địa điểm, ngôn ngữ, bản thuyết minh) => HTTP 404.
 * Các factory method giúp thông điệp lỗi nhất quán trong toàn hệ thống.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static ResourceNotFoundException location(Long id) {
        return new ResourceNotFoundException(ErrorCode.LOCATION_NOT_FOUND,
                "Không tìm thấy địa điểm với id = " + id);
    }

    public static ResourceNotFoundException language(String code) {
        return new ResourceNotFoundException(ErrorCode.LANGUAGE_NOT_FOUND,
                "Không tìm thấy ngôn ngữ với mã '" + code + "'");
    }

    public static ResourceNotFoundException narration(Long id) {
        return new ResourceNotFoundException(ErrorCode.NARRATION_NOT_FOUND,
                "Không tìm thấy bản thuyết minh với id = " + id);
    }
}
