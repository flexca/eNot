package com.github.flexca.enot.core.exception;

public class EnotInvalidConfigurationException extends EnotRuntimeException {

    public EnotInvalidConfigurationException(String message) {
        super(message);
    }

    public EnotInvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
