package com.oenexa.api.exception;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.api.exception.GlobalErrorHandler.ErrorResponse;
import com.oenexa.api.exception.GlobalErrorHandler.FieldErrorDetail;
import com.oenexa.domain.exception.ErrorCode;
import com.oenexa.domain.exception.OenexaException;
import com.oenexa.domain.exception.OenexaExceptions.OenexaNotFoundException;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalErrorHandler Industrial Zero-Mock Unit Tests")
class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    public void dummyMethod(String param) {}

    @Test
    @DisplayName("Should handle OenexaException and map to correct RFC 7807/9457 Problem Details")
    void shouldHandleOenexaException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/resources/42");
        request.addHeader("X-Correlation-Id", "custom-trace-999");

        OenexaNotFoundException ex = new OenexaNotFoundException("UserAccount", "42");
        ResponseEntity<ErrorResponse> resp = handler.handleOenexaException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("custom-trace-999", resp.getHeaders().getFirst("X-Correlation-Id"));

        ErrorResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(404, body.status());
        assertEquals("NOT_FOUND", body.code());
        assertEquals("Resource Not Found", body.title());
        assertTrue(body.message().contains("UserAccount not found with identifier: 42"));
        assertEquals("/api/v1/resources/42", body.path());
        assertNotNull(body.remediation());
        assertEquals("urn:oenexa:problem:not-found", body.type().toString());
    }

    @Test
    @DisplayName("Should handle OenexaValidationException and populate field violations")
    void shouldHandleOenexaValidationException() {
        OenexaValidationException ex = new OenexaValidationException("Invalid input payload", List.of("email is invalid", "password too short"));
        ResponseEntity<ErrorResponse> resp = handler.handleOenexaException(ex, null);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        ErrorResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(2, body.fieldErrors().size());
        assertEquals("email is invalid", body.fieldErrors().get(0).message());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with field error details")
    void shouldHandleValidationException() throws Exception {
        Method method = getClass().getMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "username", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ErrorResponse> resp = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("VALIDATION", resp.getBody().code());
        assertTrue(resp.getBody().message().contains("username must not be blank"));
        assertEquals(1, resp.getBody().fieldErrors().size());
        assertEquals("username", resp.getBody().fieldErrors().get(0).field());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException without field errors")
    void shouldHandleEmptyValidationException() throws Exception {
        Method method = getClass().getMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ErrorResponse> resp = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Validation failed", resp.getBody().message());
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException (malformed JSON)")
    void shouldHandleMalformedJsonException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error: Unexpected character", (org.springframework.http.HttpInputMessage) null);
        ResponseEntity<ErrorResponse> resp = handler.handleMalformedJson(ex, null);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("BAD_REQUEST", resp.getBody().code());
        assertTrue(resp.getBody().message().contains("Malformed JSON"));
    }

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException")
    void shouldHandleMissingParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("prompt", "String");
        ResponseEntity<ErrorResponse> resp = handler.handleMissingParameter(ex, null);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().message().contains("prompt"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException")
    void shouldHandleTypeMismatchException() throws Exception {
        Method method = getClass().getMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "limit", parameter, null);

        ResponseEntity<ErrorResponse> resp = handler.handleTypeMismatch(ex, null);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(resp.getBody().message().contains("Parameter 'limit' expected type 'Integer'"));
    }

    @Test
    @DisplayName("Should handle NoSuchElementException mapping to 404 Not Found")
    void shouldHandleNoSuchElementException() {
        NoSuchElementException ex = new NoSuchElementException("Entity not present");
        ResponseEntity<ErrorResponse> resp = handler.handleNotFound(ex, null);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("NOT_FOUND", resp.getBody().code());
        assertEquals("Entity not present", resp.getBody().message());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException mapping to 400 Bad Request")
    void shouldHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Value cannot be negative");
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalArgument(ex, null);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Value cannot be negative", resp.getBody().message());
    }

    @Test
    @DisplayName("Should handle IllegalStateException mapping to 409 Conflict")
    void shouldHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Account already verified");
        ResponseEntity<ErrorResponse> resp = handler.handleIllegalState(ex, null);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals("CONFLICT", resp.getBody().code());
        assertEquals("Account already verified", resp.getBody().message());
    }

    @Test
    @DisplayName("Should handle SecurityException mapping to 401 Unauthorized")
    void shouldHandleSecurityException() {
        SecurityException ex = new SecurityException("Invalid JWT signature");
        ResponseEntity<ErrorResponse> resp = handler.handleSecurityException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("UNAUTHORIZED", resp.getBody().code());
        assertEquals("Invalid JWT signature", resp.getBody().message());
    }

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException mapping to 405 Method Not Allowed")
    void shouldHandleMethodNotAllowed() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");
        ResponseEntity<ErrorResponse> resp = handler.handleMethodNotAllowed(ex, null);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp.getStatusCode());
        assertEquals("METHOD_NOT_ALLOWED", resp.getBody().code());
    }

    @Test
    @DisplayName("Should handle HttpMediaTypeNotSupportedException mapping to 415")
    void shouldHandleMediaTypeNotSupported() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("application/xml");
        ResponseEntity<ErrorResponse> resp = handler.handleMediaTypeNotSupported(ex, null);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, resp.getStatusCode());
        assertEquals("UNSUPPORTED_MEDIA_TYPE", resp.getBody().code());
    }

    @Test
    @DisplayName("Should handle TimeoutException mapping to 504 Gateway Timeout")
    void shouldHandleTimeoutException() {
        TimeoutException ex = new TimeoutException("Autonomous execution timed out");
        ResponseEntity<ErrorResponse> resp = handler.handleTimeout(ex, null);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, resp.getStatusCode());
        assertEquals("GATEWAY_TIMEOUT", resp.getBody().code());
    }

    @Test
    @DisplayName("Should handle generic Exception and safely sanitize internal details in 500 responses")
    void shouldHandleGenericException() {
        RuntimeException ex = new RuntimeException("Fatal SQL constraint: select * from secret_keys");
        ResponseEntity<ErrorResponse> resp = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("INTERNAL", resp.getBody().code());
        assertEquals("Internal platform error", resp.getBody().message());
        assertFalse(resp.getBody().message().contains("secret_keys"), "Must never leak sensitive SQL or system secrets");
        assertNotNull(resp.getBody().traceId());
    }

    @Test
    @DisplayName("Should verify FieldErrorDetail and ErrorResponse constructors")
    void shouldVerifyRecordsConstructors() {
        FieldErrorDetail fed = new FieldErrorDetail("field", "val", "msg");
        assertEquals("field", fed.field());
        assertEquals("val", fed.rejectedValue());
        assertEquals("msg", fed.message());

        ErrorResponse er = new ErrorResponse("CODE_1", "Msg 1", java.time.Instant.now());
        assertEquals("CODE_1", er.code());
        assertEquals("Msg 1", er.message());
        assertNotNull(er.timestamp());
        assertNotNull(er.traceId());
    }
}


