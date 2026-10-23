package com.example.multilingualnarration.exception;

/**
 * Lỗi liên quan đến dịch vụ tích hợp bên ngoài (AI dịch thuật, Text-to-Speech):
 * <ul>
 *   <li>Chưa cấu hình provider => 503 (TRANSLATION_PROVIDER_UNAVAILABLE / TTS_PROVIDER_UNAVAILABLE)</li>
 *   <li>Provider gọi thất bại => 502 (TRANSLATION_FAILED / TTS_FAILED)</li>
 * </ul>
 * Thông điệp trả cho client luôn chung chung; chi tiết lỗi gốc chỉ ghi vào log server.
 */
public class IntegrationException extends BusinessException {

    public IntegrationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public IntegrationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
