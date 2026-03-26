package com.github.flexca.enot.core.exception;

public abstract class EnotRuntimeException extends RuntimeException {

    protected EnotRuntimeException(String message) {
        super(message);
    }

    protected EnotRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
