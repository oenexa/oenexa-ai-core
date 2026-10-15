package com.oenexa.domain;

/**
 * Standard checked/runtime business exception with typed ErrorCode.
 */
public class OenexaException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final ErrorCode code;

    public OenexaException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public OenexaException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
