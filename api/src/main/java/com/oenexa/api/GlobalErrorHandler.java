package com.oenexa.api;

import com.oenexa.domain.ErrorCode;
import com.oenexa.domain.OenexaException;
import com.oenexa.domain.OenexaExceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Enterprise Global Exception & Error Handler for OENEXA.
 * Standardizes all platform errors into uniform RFC 7807 / RFC 9457 Problem Details envelopes,
 * enforces non-leaking security boundaries, and instruments end-to-end trace correlation.
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    public record FieldErrorDetail(String field, Object rejectedValue, String message) {
        public FieldErrorDetail {
            Objects.requireNonNull(field, "field must not be null");
            message = (message != null) ? message : "";
        }
    }

    public record ErrorResponse(
            URI type,
            String title,
            int status,
            String code,
            String message,
            String path,
            String traceId,
            Instant timestamp,
            List<FieldErrorDetail> fieldErrors,
            String remediation
    ) {
        public ErrorResponse(String code, String message, Instant timestamp) {
            this(
                    URI.create("urn:oenexa:problem:" + (code != null ? code.toLowerCase().replace('_', '-') : "error")),
                    code != null ? code : "ERROR",
                    400,
                    code != null ? code : "ERROR",
                    message != null ? message : "",
                    null,
                    UUID.randomUUID().toString(),
                    timestamp != null ? timestamp : Instant.now(),
                    List.of(),
                    null
            );
        }
    }

    @ExceptionHandler(OenexaException.class)
    public ResponseEntity<ErrorResponse> handleOenexaException(OenexaException e, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = List.of();
        if (e instanceof OenexaValidationException valEx && !valEx.violations().isEmpty()) {
            fieldErrors = valEx.violations().stream()
                    .map(v -> new FieldErrorDetail("validation", null, v))
                    .toList();
        }
        return buildResponse(e.code(), e.getMessage(), e.remediation(), request, fieldErrors, e.traceId());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new FieldErrorDetail(f.getField(), f.getRejectedValue(), f.getDefaultMessage()))
                .toList();

        String summary = fieldErrors.stream()
                .map(fe -> fe.field() + " " + fe.message())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        return buildResponse(ErrorCode.VALIDATION, summary, ErrorCode.VALIDATION.remediationHint(), request, fieldErrors, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = e.getConstraintViolations().stream()
                .map(cv -> new FieldErrorDetail(cv.getPropertyPath().toString(), cv.getInvalidValue(), cv.getMessage()))
                .toList();

        return buildResponse(ErrorCode.VALIDATION, e.getMessage(), ErrorCode.VALIDATION.remediationHint(), request, fieldErrors, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.BAD_REQUEST, "Malformed JSON request body: syntax or type conversion failure", "Verify JSON structure and data types against schema.", request, List.of(), null);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ErrorResponse> handleMissingParameter(Exception e, HttpServletRequest request) {
        return buildResponse(ErrorCode.BAD_REQUEST, e.getMessage(), "Provide all mandatory parameters and headers.", request, List.of(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String msg = String.format("Parameter '%s' expected type '%s'", e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");
        return buildResponse(ErrorCode.BAD_REQUEST, msg, "Ensure parameter format matches target type.", request, List.of(), null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.NOT_FOUND, e.getMessage(), ErrorCode.NOT_FOUND.remediationHint(), request, List.of(), null);
    }

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgument(RuntimeException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.BAD_REQUEST, e.getMessage(), "Review input argument constraints.", request, List.of(), null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.CONFLICT, e.getMessage(), "Check current resource lifecycle state before retrying.", request, List.of(), null);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.UNAUTHORIZED, e.getMessage(), ErrorCode.UNAUTHORIZED.remediationHint(), request, List.of(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, e.getMessage(), ErrorCode.METHOD_NOT_ALLOWED.remediationHint(), request, List.of(), null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, e.getMessage(), ErrorCode.UNSUPPORTED_MEDIA_TYPE.remediationHint(), request, List.of(), null);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(TimeoutException e, HttpServletRequest request) {
        return buildResponse(ErrorCode.GATEWAY_TIMEOUT, "Operation timed out", ErrorCode.GATEWAY_TIMEOUT.remediationHint(), request, List.of(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
        String traceId = resolveTraceId(request);
        log.error("Unhandled platform fault [traceId={}]: {}", traceId, e.getMessage(), e);
        return buildResponse(ErrorCode.INTERNAL, "Internal platform error", "Please contact support with trace ID: " + traceId, request, List.of(), traceId);
    }

    // Overloads for programmatic and unit test invocation
    public ResponseEntity<ErrorResponse> handleOenexaException(OenexaException e) {
        return handleOenexaException(e, null);
    }

    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        return handleValidationException(e, null);
    }

    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
        return handleSecurityException(e, null);
    }

    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        return handleGenericException(e, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode code,
            String message,
            String remediation,
            HttpServletRequest request,
            List<FieldErrorDetail> fieldErrors,
            String fallbackTraceId
    ) {
        String traceId = resolveTraceId(request, fallbackTraceId);
        String path = (request != null) ? request.getRequestURI() : null;

        ErrorResponse body = new ErrorResponse(
                code.problemTypeUri(),
                code.defaultTitle(),
                code.httpStatus(),
                code.name(),
                message != null ? message : code.defaultTitle(),
                path,
                traceId,
                Instant.now(),
                fieldErrors != null ? List.copyOf(fieldErrors) : List.of(),
                remediation != null ? remediation : code.remediationHint()
        );

        return ResponseEntity.status(HttpStatus.valueOf(code.httpStatus()))
                .header(CORRELATION_HEADER, traceId)
                .body(body);
    }

    private String resolveTraceId(HttpServletRequest request) {
        return resolveTraceId(request, null);
    }

    private String resolveTraceId(HttpServletRequest request, String fallbackTraceId) {
        if (request != null) {
            String traceId = request.getHeader(CORRELATION_HEADER);
            if (traceId != null && !traceId.isBlank()) return traceId;
            traceId = request.getHeader("X-Request-Id");
            if (traceId != null && !traceId.isBlank()) return traceId;
        }
        if (fallbackTraceId != null && !fallbackTraceId.isBlank()) {
            return fallbackTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
