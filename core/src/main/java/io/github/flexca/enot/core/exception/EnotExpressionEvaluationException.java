package io.github.flexca.enot.core.exception;

/**
 * Thrown when a condition expression cannot be evaluated at serialization time.
 *
 * <p>Evaluation fails when operand types are incompatible (e.g. comparing a string
 * placeholder against an integer literal with {@code >}), when {@code date_time()}
 * receives a value that cannot be parsed as a date-time string, or when the final
 * expression result is not a boolean.
 *
 * <p>This is a checked exception wrapping expression-engine failures. The serializer
 * typically wraps it inside an {@link EnotSerializationException}.
 */
public class EnotExpressionEvaluationException extends EnotException {

    public EnotExpressionEvaluationException(String message) {
        super(message);
    }

    public EnotExpressionEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
