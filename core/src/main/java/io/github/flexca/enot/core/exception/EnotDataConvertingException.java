package io.github.flexca.enot.core.exception;

/**
 * Thrown when a value-type converter fails to convert a value to binary.
 *
 * <p>This surfaces when the binary converter registered for an
 * {@link io.github.flexca.enot.core.element.value.EnotValueType} encounters a value
 * of an unexpected Java type or malformed content — for example, a string that
 * cannot be parsed as a valid OID, or an integer that overflows the target encoding.
 *
 * <p>This is a runtime (unchecked) exception.
 */
public class EnotDataConvertingException extends EnotRuntimeException {

    public EnotDataConvertingException(String message) {
        super(message);
    }

    public EnotDataConvertingException(String message, Throwable cause) {
        super(message, cause);
    }
}
