package com.example.multilingualnarration.exception;

import com.example.multilingualnarration.dto.response.ErrorResponse;
import com.example.multilingualnarration.dto.response.ErrorResponse.FieldErrorDetail;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Xử lý lỗi TẬP TRUNG cho toàn bộ Controller.
 *
 * <p>{@code @RestControllerAdvice}: Spring áp dụng class này cho mọi {@code @RestController}.
 * Khi một exception "bay" ra khỏi Controller, Spring tìm method {@code @ExceptionHandler}
 * phù hợp nhất (theo kiểu exception) để chuyển thành response JSON.
 *
 * <p>Lợi ích: Controller/Service không cần try-catch; mọi lỗi có cùng định dạng
 * {@link ErrorResponse}; KHÔNG BAO GIỜ lộ stack trace, câu SQL hay tên bảng cho client
 * (chi tiết kỹ thuật chỉ ghi vào log phía server).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ------------------------------------------------------------------
    // 1. Lỗi nghiệp vụ do tầng Service chủ động ném ra (404, 409, 400, 502, 503...)
    // ------------------------------------------------------------------
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        if (code.getHttpStatus().is5xxServerError()) {
            log.warn("Lỗi tích hợp/hệ thống [{}] tại {}: {}", code, request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.debug("Lỗi nghiệp vụ [{}] tại {}: {}", code, request.getRequestURI(), ex.getMessage());
        }
        return build(code, ex.getMessage(), request, null);
    }

    // ------------------------------------------------------------------
    // 2. Lỗi validation
    // ------------------------------------------------------------------

    /** Body JSON vi phạm annotation (@NotBlank, @Size...) trong Request DTO có {@code @Valid}. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<FieldErrorDetail> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                details.add(new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(globalError ->
                details.add(new FieldErrorDetail(globalError.getObjectName(), globalError.getDefaultMessage())));
        return build(ErrorCode.VALIDATION_ERROR, "Dữ liệu không hợp lệ", request, details);
    }

    /**
     * Query param / path variable vi phạm ràng buộc (ví dụ size > 100, keyword rỗng).
     * Từ Spring Framework 6.1, Spring MVC tự kiểm tra các annotation đặt trực tiếp trên
     * tham số của method Controller và ném exception này.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleParameterValidation(HandlerMethodValidationException ex,
                                                                   HttpServletRequest request) {
        List<FieldErrorDetail> details = new ArrayList<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                // Tham số là object (ví dụ @RequestBody) => lấy lỗi theo từng field
                parameterErrors.getFieldErrors().forEach(fieldError ->
                        details.add(new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage())));
            } else {
                String parameterName = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error ->
                        details.add(new FieldErrorDetail(parameterName, error.getDefaultMessage())));
            }
        }
        return build(ErrorCode.VALIDATION_ERROR, "Tham số không hợp lệ", request, details);
    }

    /** Dự phòng cho trường hợp validation ở mức method được kích hoạt bằng {@code @Validated}. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        List<FieldErrorDetail> details = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            details.add(new FieldErrorDetail(field, violation.getMessage()));
        }
        return build(ErrorCode.VALIDATION_ERROR, "Tham số không hợp lệ", request, details);
    }

    // ------------------------------------------------------------------
    // 3. Request sai định dạng
    // ------------------------------------------------------------------

    /** Body không phải JSON hợp lệ, hoặc giá trị sai kiểu (ví dụ status = "ABC"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        String message = "Body của request không đúng định dạng JSON hoặc sai kiểu dữ liệu";
        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {
            String field = lastFieldName(invalidFormat.getPath());
            Class<?> targetType = invalidFormat.getTargetType();
            if (targetType != null && targetType.isEnum()) {
                message = "Giá trị '" + invalidFormat.getValue() + "' không hợp lệ cho trường '" + field
                        + "'. Giá trị cho phép: " + Arrays.toString(targetType.getEnumConstants());
            } else {
                message = "Giá trị '" + invalidFormat.getValue() + "' sai kiểu dữ liệu cho trường '" + field + "'";
            }
        }
        return build(ErrorCode.INVALID_REQUEST, message, request, null);
    }

    /** Path variable / query param sai kiểu, ví dụ /api/v1/locations/abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = "Giá trị '" + ex.getValue() + "' không hợp lệ cho tham số '" + ex.getName() + "'";
        return build(ErrorCode.INVALID_REQUEST, message, request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                HttpServletRequest request) {
        String message = "Thiếu tham số bắt buộc '" + ex.getParameterName() + "'";
        return build(ErrorCode.INVALID_REQUEST, message, request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest request) {
        String message = "Phương thức " + ex.getMethod() + " không được hỗ trợ cho đường dẫn này";
        return build(ErrorCode.METHOD_NOT_ALLOWED, message, request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpServletRequest request) {
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                "Content-Type không được hỗ trợ, hãy dùng application/json", request, null);
    }

    /** Gọi tới đường dẫn không tồn tại. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return build(ErrorCode.ENDPOINT_NOT_FOUND, "Không tồn tại đường dẫn này", request, null);
    }

    // ------------------------------------------------------------------
    // 4. Lỗi ràng buộc database (unique, foreign key...) lọt qua bước kiểm tra của Service
    //    (ví dụ 2 request tạo trùng gửi đến cùng lúc). Không trả câu SQL / tên constraint cho client.
    // ------------------------------------------------------------------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest request) {
        log.warn("Vi phạm ràng buộc dữ liệu tại {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.DATA_CONFLICT,
                "Dữ liệu bị trùng hoặc vi phạm ràng buộc toàn vẹn, vui lòng kiểm tra lại", request, null);
    }

    // ------------------------------------------------------------------
    // 5. Mọi lỗi chưa lường trước => 500, thông điệp chung chung, ghi đầy đủ vào log
    // ------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không mong muốn tại {}", request.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_ERROR, "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau", request, null);
    }

    // ------------------------------------------------------------------
    private ResponseEntity<ErrorResponse> build(ErrorCode code, String message, HttpServletRequest request,
                                                List<FieldErrorDetail> details) {
        HttpStatus status = code.getHttpStatus();
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code.name(),
                message,
                request.getRequestURI(),
                details);
        return ResponseEntity.status(status).body(body);
    }

    private static String lastFieldName(List<JsonMappingException.Reference> path) {
        if (path == null || path.isEmpty()) {
            return "?";
        }
        String fieldName = path.get(path.size() - 1).getFieldName();
        return fieldName != null ? fieldName : "?";
    }
}
