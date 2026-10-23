package com.oenexa.domain.exception;

import com.oenexa.domain.ErrorCode;
import com.oenexa.domain.OenexaException;

/**
 * Domain-specific exception for Autonomous Agent and Cognitive Brain failures.
 */
public class AgentGoalDomainException extends OenexaException {
    private static final long serialVersionUID = 1L;

    public AgentGoalDomainException(String message) {
        super(ErrorCode.COGNITIVE_FAULT, message);
    }

    public AgentGoalDomainException(String message, String remediation) {
        super(ErrorCode.COGNITIVE_FAULT, message, remediation);
    }

    public AgentGoalDomainException(ErrorCode code, String message) {
        super(code != null ? code : ErrorCode.COGNITIVE_FAULT, message);
    }
}
