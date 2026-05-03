package io.github.flexca.enot.core.exception;

/**
 * Base class for all unchecked (runtime) exceptions thrown by the eNot library.
 *
 * <p>Runtime exceptions indicate programming errors or misconfiguration that the
 * caller is not expected to recover from at runtime:
 * <ul>
 *   <li>{@link EnotInvalidConfigurationException} — incorrect
 *       {@link io.github.flexca.enot.core.registry.EnotRegistry} setup.</li>
 *   <li>{@link EnotInvalidArgumentException} — a blank or otherwise invalid argument
 *       was passed to an API method.</li>
 *   <li>{@link EnotDataConvertingException} — a value-type converter produced an
 *       unexpected result.</li>
 * </ul>
 */
public abstract class EnotRuntimeException extends RuntimeException {

    protected EnotRuntimeException(String message) {
        super(message);
    }

    protected EnotRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
