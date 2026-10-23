package com.oenexa.domain.exception;

import com.oenexa.domain.ErrorCode;
import com.oenexa.domain.OenexaException;

/**
 * Domain-specific exception for Authentication and Identity failures.
 */
public class AuthDomainException extends OenexaException {
    private static final long serialVersionUID = 1L;

    public AuthDomainException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public AuthDomainException(String message, String remediation) {
        super(ErrorCode.UNAUTHORIZED, message, remediation);
    }

    public AuthDomainException(ErrorCode code, String message) {
        super(code != null ? code : ErrorCode.UNAUTHORIZED, message);
    }
}
