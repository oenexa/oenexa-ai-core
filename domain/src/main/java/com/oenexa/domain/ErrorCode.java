package com.oenexa.domain;

import java.net.URI;

/**
 * Standardized industrial error codes for platform failures across all modules.
 * Fully compliant with RFC 7807 / RFC 9457 Problem Details specification.
 * Pure Java enum with zero external framework dependencies in Domain.
 */
public enum ErrorCode {
    VALIDATION(400, "Validation Failed", "Check field-level requirements and constraint annotations."),
    BAD_REQUEST(400, "Bad Request", "Inspect request structure, JSON syntax, and query parameters."),
    UNAUTHORIZED(401, "Authentication Required", "Authenticate using valid credentials or Bearer token."),
    FORBIDDEN(403, "Access Forbidden", "Principal lacks required role or permission for this resource."),
    NOT_FOUND(404, "Resource Not Found", "Verify resource identifier or URL path existence."),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed", "Use one of the supported HTTP methods for this endpoint."),
    CONFLICT(409, "Resource State Conflict", "Resolve conflicting resource state, version, or concurrent modification."),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type", "Supply Content-Type header matching application/json or text/event-stream."),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity", "Resolve domain business invariant violations in payload."),
    RATE_LIMITED(429, "Rate Limit Exceeded", "Reduce request frequency or implement exponential backoff."),
    INTERNAL(500, "Internal Server Error", "Contact platform support or retry with correlation trace ID."),
    COGNITIVE_FAULT(500, "Cognitive Reasoning Fault", "Autonomous deliberative pipeline encountered an internal state fault."),
    UPSTREAM_FAILURE(502, "Upstream Service Failure", "Check upstream inference server or external dependency health."),
    SERVICE_UNAVAILABLE(503, "Service Unavailable", "Platform is temporarily unavailable; retry after backoff interval."),
    GATEWAY_TIMEOUT(504, "Gateway Timeout", "Autonomous task execution exceeded maximum time budget.");

    private final int httpStatus;
    private final String defaultTitle;
    private final String remediationHint;

    ErrorCode(int httpStatus, String defaultTitle, String remediationHint) {
        this.httpStatus = httpStatus;
        this.defaultTitle = defaultTitle;
        this.remediationHint = remediationHint;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String defaultTitle() {
        return defaultTitle;
    }

    public String remediationHint() {
        return remediationHint;
    }

    public URI problemTypeUri() {
        return URI.create("urn:oenexa:problem:" + name().toLowerCase().replace('_', '-'));
    }

    public boolean isClientError() {
        return httpStatus >= 400 && httpStatus < 500;
    }

    public boolean isServerError() {
        return httpStatus >= 500 && httpStatus < 600;
    }
}
