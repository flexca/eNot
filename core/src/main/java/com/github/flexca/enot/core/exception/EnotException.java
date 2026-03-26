package com.github.flexca.enot.core.exception;

public class EnotException extends Exception {

    protected EnotException(String message) {
        super(message);
    }

    protected EnotException(String message, Throwable cause) {
        super(message, cause);
    }
}
