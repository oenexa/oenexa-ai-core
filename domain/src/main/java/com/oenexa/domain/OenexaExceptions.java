package com.oenexa.domain;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise typed domain exceptions for OENEXA hexagonal architecture.
 * Provides specialized error subclasses for each distinct fault domain.
 */
public final class OenexaExceptions {

    private OenexaExceptions() {}

    public static class OenexaValidationException extends OenexaException {
        private static final long serialVersionUID = 1L;
        private final List<String> violations;

        public OenexaValidationException(String message) {
            this(message, List.of());
        }

        public OenexaValidationException(String message, List<String> violations) {
            super(ErrorCode.VALIDATION, message);
            this.violations = violations != null ? List.copyOf(violations) : List.of();
        }

        public List<String> violations() {
            return violations;
        }
    }

    public static class OenexaNotFoundException extends OenexaException {
        private static final long serialVersionUID = 1L;
        private final String resource;
        private final String identifier;

        public OenexaNotFoundException(String resource, String identifier) {
            super(
                    ErrorCode.NOT_FOUND,
                    String.format("%s not found with identifier: %s", Objects.requireNonNull(resource, "resource must not be null"), identifier),
                    "Verify the resource identifier exists and has not been deleted."
            );
            this.resource = resource;
            this.identifier = identifier != null ? identifier : "UNKNOWN";
        }

        public String resource() {
            return resource;
        }

        public String identifier() {
            return identifier;
        }
    }

    public static class OenexaAuthenticationException extends OenexaException {
        private static final long serialVersionUID = 1L;

        public OenexaAuthenticationException(String message) {
            super(ErrorCode.UNAUTHORIZED, message, "Provide a valid Authorization Bearer token or sign in again.");
        }
    }

    public static class OenexaForbiddenException extends OenexaException {
        private static final long serialVersionUID = 1L;
        private final String requiredRole;

        public OenexaForbiddenException(String message, String requiredRole) {
            super(ErrorCode.FORBIDDEN, message, "Contact an administrator to request role: " + requiredRole);
            this.requiredRole = requiredRole != null ? requiredRole : "ROLE_ADMIN";
        }

        public String requiredRole() {
            return requiredRole;
        }
    }

    public static class OenexaConflictException extends OenexaException {
        private static final long serialVersionUID = 1L;
        private final String conflictingKey;

        public OenexaConflictException(String message, String conflictingKey) {
            super(ErrorCode.CONFLICT, message, "Resolve the conflicting duplicate attribute: " + conflictingKey);
            this.conflictingKey = conflictingKey != null ? conflictingKey : "UNKNOWN";
        }

        public String conflictingKey() {
            return conflictingKey;
        }
    }

    public static class OenexaRateLimitException extends OenexaException {
        private static final long serialVersionUID = 1L;
        private final long retryAfterSeconds;

        public OenexaRateLimitException(String message, long retryAfterSeconds) {
            super(ErrorCode.RATE_LIMITED, message, String.format("Rate limit reached. Retry after %d seconds.", retryAfterSeconds));
            this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
        }

        public long retryAfterSeconds() {
            return retryAfterSeconds;
        }
    }

    public static class OenexaUpstreamException extends OenexaException {
        private static final long serialVersionUID = 1L;
        private final String upstreamService;

        public OenexaUpstreamException(String upstreamService, String message, Throwable cause) {
            super(
                    ErrorCode.UPSTREAM_FAILURE,
                    String.format("Upstream service failure [%s]: %s", upstreamService, message),
                    "Verify network connectivity and health of upstream service: " + upstreamService,
                    null,
                    cause
            );
            this.upstreamService = upstreamService != null ? upstreamService : "UPSTREAM";
        }

        public String upstreamService() {
            return upstreamService;
        }
    }

    public static class OenexaCognitiveExecutionException extends OenexaException {
        private static final long serialVersionUID = 1L;

        public OenexaCognitiveExecutionException(String message, Throwable cause) {
            super(ErrorCode.COGNITIVE_FAULT, message, "Check cognitive model context and prompt parameters.", null, cause);
        }
    }

    public static class OenexaTimeoutException extends OenexaException {
        private static final long serialVersionUID = 1L;
        private final long timeoutMs;

        public OenexaTimeoutException(String message, long timeoutMs) {
            super(ErrorCode.GATEWAY_TIMEOUT, message, String.format("Operation timed out after %d ms; simplify prompt or increase budget.", timeoutMs));
            this.timeoutMs = timeoutMs;
        }

        public long timeoutMs() {
            return timeoutMs;
        }
    }
}
