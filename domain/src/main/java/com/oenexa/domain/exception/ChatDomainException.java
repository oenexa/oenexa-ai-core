package com.oenexa.domain.exception;

import com.oenexa.domain.exception.ErrorCode;
import com.oenexa.domain.exception.OenexaException;

/**
 * Domain-specific exception for Chat, Conversation, and Prompt Safety failures.
 */
public class ChatDomainException extends OenexaException {
    private static final long serialVersionUID = 1L;

    public ChatDomainException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

    public ChatDomainException(String message, String remediation) {
        super(ErrorCode.BAD_REQUEST, message, remediation);
    }

    public ChatDomainException(ErrorCode code, String message) {
        super(code != null ? code : ErrorCode.BAD_REQUEST, message);
    }
}

