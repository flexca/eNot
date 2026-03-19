package com.github.flexca.enot.core.exception;

public abstract class EnotException extends RuntimeException {

    protected EnotException(String message) {
        super(message);
    }

    protected EnotException(String message, Throwable cause) {
        super(message, cause);
    }
}
