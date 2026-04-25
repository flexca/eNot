package io.github.flexca.enot.core.exception;

public class EnotInvalidArgumentException extends EnotRuntimeException {

    public EnotInvalidArgumentException(String message) {
        super(message);
    }

    public EnotInvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
