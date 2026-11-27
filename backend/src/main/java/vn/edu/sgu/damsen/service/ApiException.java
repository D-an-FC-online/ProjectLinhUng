package vn.edu.sgu.damsen.service;

import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ trả về cho client kèm mã HTTP và thông báo tiếng Việt. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static ApiException notFound(String what) {
        return new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy " + what);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }
}
