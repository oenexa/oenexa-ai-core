package com.oenexa.api;

import com.oenexa.domain.ErrorCode;
import com.oenexa.domain.OenexaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Global Exception Handler mapping exceptions to uniform, non-leaking error envelopes.
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    public record ErrorResponse(String code, String message, Instant timestamp) {}

    @ExceptionHandler(OenexaException.class)
    public ResponseEntity<ErrorResponse> handleOenexaException(OenexaException e) {
        return ResponseEntity.status(e.code().httpStatus())
                .body(new ErrorResponse(e.code().name(), e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.VALIDATION.name(), message, Instant.now()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.httpStatus())
                .body(new ErrorResponse(ErrorCode.UNAUTHORIZED.name(), e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unhandled error: ", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(ErrorCode.INTERNAL.name(), "Internal platform error", Instant.now()));
    }
}
