package io.github.flexca.enot.core.exception;

/**
 * Thrown when an {@link io.github.flexca.enot.core.registry.EnotRegistry} is built
 * with an invalid configuration.
 *
 * <p>Common causes include a registered
 * {@link io.github.flexca.enot.core.registry.EnotTypeSpecification} with a blank type
 * name, a duplicate custom value-type name across two specifications, or a value-type
 * converter that returns {@code null}.
 *
 * <p>This is a runtime (unchecked) exception — it indicates a programming error in
 * the application setup code that must be fixed before the application can run correctly.
 */
public class EnotInvalidConfigurationException extends EnotRuntimeException {

    public EnotInvalidConfigurationException(String message) {
        super(message);
    }

    public EnotInvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
