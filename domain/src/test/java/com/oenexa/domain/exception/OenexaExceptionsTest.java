package com.oenexa.domain.exception;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import com.oenexa.domain.exception.OenexaExceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorCode and OenexaExceptions Zero-Mock Unit Tests")
class OenexaExceptionsTest {

    @Test
    @DisplayName("Should verify ErrorCode HTTP mappings, titles, and URI formats")
    void shouldVerifyErrorCodeProperties() {
        assertEquals(400, ErrorCode.VALIDATION.httpStatus());
        assertEquals(401, ErrorCode.UNAUTHORIZED.httpStatus());
        assertEquals(403, ErrorCode.FORBIDDEN.httpStatus());
        assertEquals(404, ErrorCode.NOT_FOUND.httpStatus());
        assertEquals(405, ErrorCode.METHOD_NOT_ALLOWED.httpStatus());
        assertEquals(409, ErrorCode.CONFLICT.httpStatus());
        assertEquals(415, ErrorCode.UNSUPPORTED_MEDIA_TYPE.httpStatus());
        assertEquals(422, ErrorCode.UNPROCESSABLE_ENTITY.httpStatus());
        assertEquals(429, ErrorCode.RATE_LIMITED.httpStatus());
        assertEquals(500, ErrorCode.INTERNAL.httpStatus());
        assertEquals(500, ErrorCode.COGNITIVE_FAULT.httpStatus());
        assertEquals(502, ErrorCode.UPSTREAM_FAILURE.httpStatus());
        assertEquals(503, ErrorCode.SERVICE_UNAVAILABLE.httpStatus());
        assertEquals(504, ErrorCode.GATEWAY_TIMEOUT.httpStatus());

        assertTrue(ErrorCode.VALIDATION.isClientError());
        assertFalse(ErrorCode.VALIDATION.isServerError());
        assertTrue(ErrorCode.INTERNAL.isServerError());
        assertFalse(ErrorCode.INTERNAL.isClientError());

        assertEquals("urn:oenexa:problem:validation", ErrorCode.VALIDATION.problemTypeUri().toString());
        assertEquals("urn:oenexa:problem:rate-limited", ErrorCode.RATE_LIMITED.problemTypeUri().toString());
        assertNotNull(ErrorCode.VALIDATION.defaultTitle());
        assertNotNull(ErrorCode.VALIDATION.remediationHint());
    }

    @Test
    @DisplayName("Should instantiate base OenexaException with correlation ID and timestamp")
    void shouldInstantiateBaseOenexaException() {
        OenexaException ex = new OenexaException(ErrorCode.BAD_REQUEST, "Malformed request", "Check syntax", "trace-123");

        assertEquals(ErrorCode.BAD_REQUEST, ex.code());
        assertEquals("Malformed request", ex.getMessage());
        assertEquals("Check syntax", ex.remediation());
        assertEquals("trace-123", ex.traceId());
        assertNotNull(ex.timestamp());

        // Default constructor generates traceId
        OenexaException ex2 = new OenexaException(ErrorCode.NOT_FOUND, "Missing key");
        assertNotNull(ex2.traceId());
        assertEquals(ErrorCode.NOT_FOUND.remediationHint(), ex2.remediation());

        // With cause
        IllegalArgumentException cause = new IllegalArgumentException("Invalid value");
        OenexaException ex3 = new OenexaException(ErrorCode.INTERNAL, "Crash", cause);
        assertEquals(cause, ex3.getCause());
    }

    @Test
    @DisplayName("Should verify OenexaValidationException and violations list")
    void shouldVerifyValidationException() {
        OenexaValidationException ex = new OenexaValidationException("Invalid input", List.of("name is required", "email is invalid"));

        assertEquals(ErrorCode.VALIDATION, ex.code());
        assertEquals("Invalid input", ex.getMessage());
        assertEquals(2, ex.violations().size());
        assertTrue(ex.violations().contains("name is required"));
    }

    @Test
    @DisplayName("Should verify OenexaNotFoundException properties")
    void shouldVerifyNotFoundException() {
        OenexaNotFoundException ex = new OenexaNotFoundException("UserAccount", "user-42");

        assertEquals(ErrorCode.NOT_FOUND, ex.code());
        assertEquals("UserAccount", ex.resource());
        assertEquals("user-42", ex.identifier());
        assertTrue(ex.getMessage().contains("UserAccount not found with identifier: user-42"));
    }

    @Test
    @DisplayName("Should verify OenexaAuthenticationException and ForbiddenException")
    void shouldVerifyAuthExceptions() {
        OenexaAuthenticationException authEx = new OenexaAuthenticationException("Token expired");
        assertEquals(ErrorCode.UNAUTHORIZED, authEx.code());
        assertTrue(authEx.remediation().contains("Authorization Bearer token"));

        OenexaForbiddenException forbidEx = new OenexaForbiddenException("Access denied", "ROLE_LEAD_ARCHITECT");
        assertEquals(ErrorCode.FORBIDDEN, forbidEx.code());
        assertEquals("ROLE_LEAD_ARCHITECT", forbidEx.requiredRole());
        assertTrue(forbidEx.remediation().contains("ROLE_LEAD_ARCHITECT"));
    }

    @Test
    @DisplayName("Should verify OenexaConflictException properties")
    void shouldVerifyConflictException() {
        OenexaConflictException ex = new OenexaConflictException("Email already taken", "email");
        assertEquals(ErrorCode.CONFLICT, ex.code());
        assertEquals("email", ex.conflictingKey());
    }

    @Test
    @DisplayName("Should verify OenexaRateLimitException and retryAfterSeconds")
    void shouldVerifyRateLimitException() {
        OenexaRateLimitException ex = new OenexaRateLimitException("Too many inferences", 30L);
        assertEquals(ErrorCode.RATE_LIMITED, ex.code());
        assertEquals(30L, ex.retryAfterSeconds());
        assertTrue(ex.remediation().contains("Retry after 30 seconds"));
    }

    @Test
    @DisplayName("Should verify OenexaUpstreamException and CognitiveExecutionException")
    void shouldVerifyUpstreamAndCognitiveExceptions() {
        RuntimeException cause = new RuntimeException("Connection refused");
        OenexaUpstreamException upEx = new OenexaUpstreamException("Ollama-Local", "Failed to connect", cause);
        assertEquals(ErrorCode.UPSTREAM_FAILURE, upEx.code());
        assertEquals("Ollama-Local", upEx.upstreamService());
        assertEquals(cause, upEx.getCause());

        OenexaCognitiveExecutionException cogEx = new OenexaCognitiveExecutionException("Decomposition looped", cause);
        assertEquals(ErrorCode.COGNITIVE_FAULT, cogEx.code());
        assertEquals(cause, cogEx.getCause());
    }

    @Test
    @DisplayName("Should verify OenexaTimeoutException properties")
    void shouldVerifyTimeoutException() {
        OenexaTimeoutException ex = new OenexaTimeoutException("Execution exceeded limit", 15000L);
        assertEquals(ErrorCode.GATEWAY_TIMEOUT, ex.code());
        assertEquals(15000L, ex.timeoutMs());
        assertTrue(ex.remediation().contains("15000 ms"));
    }
}


