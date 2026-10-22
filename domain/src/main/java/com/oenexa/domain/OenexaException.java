package com.oenexa.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Standard enterprise runtime business exception with typed ErrorCode,
 * correlation trace identification, and actionable remediation metadata.
 * Complies with strict zero-mock domain standards and RFC 9457 Problem Details.
 */
public class OenexaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;
    private final String remediation;
    private final String traceId;
    private final Instant timestamp;

    public OenexaException(ErrorCode code, String message) {
        this(code, message, code != null ? code.remediationHint() : null, null, null);
    }

    public OenexaException(ErrorCode code, String message, Throwable cause) {
        this(code, message, code != null ? code.remediationHint() : null, null, cause);
    }

    public OenexaException(ErrorCode code, String message, String remediation) {
        this(code, message, remediation, null, null);
    }

    public OenexaException(ErrorCode code, String message, String remediation, String traceId) {
        this(code, message, remediation, traceId, null);
    }

    public OenexaException(ErrorCode code, String message, String remediation, String traceId, Throwable cause) {
        super(Objects.requireNonNull(message, "message must not be null"), cause);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.remediation = (remediation != null && !remediation.isBlank()) ? remediation : code.remediationHint();
        this.traceId = (traceId != null && !traceId.isBlank()) ? traceId : UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public ErrorCode code() {
        return code;
    }

    public String remediation() {
        return remediation;
    }

    public String traceId() {
        return traceId;
    }

    public Instant timestamp() {
        return timestamp;
    }
}
