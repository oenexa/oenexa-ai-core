package com.oenexa.api;

import com.oenexa.domain.ErrorCode;
import com.oenexa.domain.OenexaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalErrorHandler Zero-Mock Unit Tests")
class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    public void dummyMethod(String param) {}

    @Test
    @DisplayName("Should handle OenexaException and map to correct HTTP status")
    void shouldHandleOenexaException() {
        OenexaException ex = new OenexaException(ErrorCode.NOT_FOUND, "Resource missing");
        ResponseEntity<GlobalErrorHandler.ErrorResponse> resp = handler.handleOenexaException(ex);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("NOT_FOUND", resp.getBody().code());
        assertEquals("Resource missing", resp.getBody().message());
        assertNotNull(resp.getBody().timestamp());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with field error messages")
    void shouldHandleValidationException() throws Exception {
        Method method = getClass().getMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "username", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<GlobalErrorHandler.ErrorResponse> resp = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("VALIDATION", resp.getBody().code());
        assertTrue(resp.getBody().message().contains("username must not be blank"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException without field errors")
    void shouldHandleEmptyValidationException() throws Exception {
        Method method = getClass().getMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<GlobalErrorHandler.ErrorResponse> resp = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Validation failed", resp.getBody().message());
    }

    @Test
    @DisplayName("Should handle SecurityException and map to 401 Unauthorized")
    void shouldHandleSecurityException() {
        SecurityException ex = new SecurityException("Invalid JWT token");
        ResponseEntity<GlobalErrorHandler.ErrorResponse> resp = handler.handleSecurityException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("UNAUTHORIZED", resp.getBody().code());
        assertEquals("Invalid JWT token", resp.getBody().message());
    }

    @Test
    @DisplayName("Should handle generic Exception and map to 500 Internal Server Error")
    void shouldHandleGenericException() {
        RuntimeException ex = new RuntimeException("Unexpected database failure");
        ResponseEntity<GlobalErrorHandler.ErrorResponse> resp = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("INTERNAL", resp.getBody().code());
        assertEquals("Internal platform error", resp.getBody().message());
    }
}
