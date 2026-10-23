package com.oenexa.domain.exception;

import com.oenexa.domain.exception.ErrorCode;
import com.oenexa.domain.exception.OenexaException;

/**
 * Domain-specific exception for Code Intelligence, AST analysis, and SAST security failures.
 */
public class CodeEngineeringDomainException extends OenexaException {
    private static final long serialVersionUID = 1L;

    public CodeEngineeringDomainException(String message) {
        super(ErrorCode.VALIDATION, message);
    }

    public CodeEngineeringDomainException(String message, String remediation) {
        super(ErrorCode.VALIDATION, message, remediation);
    }

    public CodeEngineeringDomainException(ErrorCode code, String message) {
        super(code != null ? code : ErrorCode.VALIDATION, message);
    }
}

