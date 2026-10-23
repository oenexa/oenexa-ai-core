package com.oenexa.domain.exception;

import com.oenexa.domain.ErrorCode;
import com.oenexa.domain.OenexaException;

/**
 * Domain-specific exception for Knowledge Base and RAG failures.
 */
public class KnowledgeDomainException extends OenexaException {
    private static final long serialVersionUID = 1L;

    public KnowledgeDomainException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public KnowledgeDomainException(String message, String remediation) {
        super(ErrorCode.NOT_FOUND, message, remediation);
    }

    public KnowledgeDomainException(ErrorCode code, String message) {
        super(code != null ? code : ErrorCode.NOT_FOUND, message);
    }
}
