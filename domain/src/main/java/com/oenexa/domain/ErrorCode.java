package com.oenexa.domain;

/**
 * Standardized error codes for platform failures across all modules.
 * Pure Java enum with HTTP status integer codes (zero Spring Framework dependency in Domain).
 */
public enum ErrorCode {
    VALIDATION(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    RATE_LIMITED(429),
    UPSTREAM_FAILURE(502),
    INTERNAL(500);

    private final int httpStatus;

    ErrorCode(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
