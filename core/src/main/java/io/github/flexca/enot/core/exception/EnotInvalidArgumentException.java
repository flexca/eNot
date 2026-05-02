package io.github.flexca.enot.core.exception;

/**
 * Thrown when a blank or otherwise invalid argument is passed to an eNot API method.
 *
 * <p>This is a runtime (unchecked) exception. It indicates a programming error —
 * the caller passed a {@code null}, blank, or structurally invalid value to a method
 * that requires a valid input.
 */
public class EnotInvalidArgumentException extends EnotRuntimeException {

    public EnotInvalidArgumentException(String message) {
        super(message);
    }

    public EnotInvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
