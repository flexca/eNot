package com.github.flexca.enot.core.exception;

public class EnotInvalidBodyException extends EnotRuntimeException {

    public EnotInvalidBodyException(String message) {
        super(message);
    }

    public EnotInvalidBodyException(String message, Throwable cause) {
        super(message, cause);
    }
}
