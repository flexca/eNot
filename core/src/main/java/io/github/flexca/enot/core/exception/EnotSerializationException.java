package io.github.flexca.enot.core.exception;

import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.util.ErrorMessageUtils;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when a parsed eNot template cannot be serialized to binary.
 *
 * <p>Serialization can fail when a required placeholder value is absent,
 * a placeholder value has the wrong type, an expression evaluates to a
 * non-boolean result, type-conversion fails, or a custom serializer throws.
 *
 * <p>The list of structured errors is available via {@link #getJsonErrors()}, where
 * each {@link EnotJsonError} carries a JSON-Pointer path to the offending template
 * node and a human-readable description.
 */
public class EnotSerializationException extends EnotException {

    private final List<EnotJsonError> jsonErrors;

    public EnotSerializationException(String message, EnotJsonError jsonError) {
        this(message, Collections.singletonList(jsonError));
    }

    public EnotSerializationException(String message, List<EnotJsonError> jsonErrors) {
        super(ErrorMessageUtils.compileParsingErrorMessage(message, jsonErrors));
        this.jsonErrors = Collections.unmodifiableList(jsonErrors);
    }

    public EnotSerializationException(String message, EnotJsonError jsonError, Throwable cause) {
        this(message, Collections.singletonList(jsonError), cause);
    }

    public EnotSerializationException(String message, List<EnotJsonError> jsonErrors, Throwable cause) {
        super(ErrorMessageUtils.compileParsingErrorMessage(message, jsonErrors), cause);
        this.jsonErrors = Collections.unmodifiableList(jsonErrors);
    }

    /**
     * Returns the structured list of serialization errors, each with a JSON-Pointer path
     * to the offending template node and a human-readable description.
     *
     * @return an unmodifiable list of errors; never {@code null}
     */
    public List<EnotJsonError> getJsonErrors() {
        return jsonErrors;
    }
}
