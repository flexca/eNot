package io.github.flexca.enot.core.exception;

/**
 * Base class for all checked exceptions thrown by the eNot library.
 *
 * <p>The two concrete checked subclasses are:
 * <ul>
 *   <li>{@link EnotParsingException} — thrown when a template cannot be parsed.</li>
 *   <li>{@link EnotSerializationException} — thrown when a parsed template cannot be serialized.</li>
 * </ul>
 *
 * <p>Callers that only need to handle any eNot failure in one {@code catch} block
 * can catch this class directly.
 */
public class EnotException extends Exception {

    protected EnotException(String message) {
        super(message);
    }

    protected EnotException(String message, Throwable cause) {
        super(message, cause);
    }
}
