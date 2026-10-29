package vn.edu.sgu.damsenguide.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        boolean isLangError = ex.getBindingResult().getFieldErrors().stream()
            .anyMatch(err -> "language".equals(err.getField()));

        String errorCode = isLangError ? "UNSUPPORTED_LANGUAGE" : "INVALID_LOCATION";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage()
        );
        problem.setProperty("code", errorCode);
        return problem;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason());
        problem.setProperty("code", ex.getReason());
        return problem;
    }
}
