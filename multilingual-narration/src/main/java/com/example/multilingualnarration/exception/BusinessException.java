package com.example.multilingualnarration.exception;

/**
 * Exception gốc cho mọi lỗi nghiệp vụ "có chủ đích" (đã lường trước) trong tầng Service.
 *
 * <p>Là RuntimeException (unchecked) nên không phải khai báo {@code throws} khắp nơi,
 * và khi ném ra trong method {@code @Transactional}, Spring tự ROLLBACK transaction.
 *
 * <p>GlobalExceptionHandler bắt exception này và chuyển thành ErrorResponse với HTTP status
 * lấy từ {@link ErrorCode}.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
