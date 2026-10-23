package com.oenexa.domain.exception;

import com.oenexa.domain.ErrorCode;
import com.oenexa.domain.OenexaException;

/**
 * Domain-specific exception for Hierarchical Memory Engine failures.
 */
public class MemoryDomainException extends OenexaException {
    private static final long serialVersionUID = 1L;

    public MemoryDomainException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public MemoryDomainException(String message, String remediation) {
        super(ErrorCode.NOT_FOUND, message, remediation);
    }

    public MemoryDomainException(ErrorCode code, String message) {
        super(code != null ? code : ErrorCode.NOT_FOUND, message);
    }
}
