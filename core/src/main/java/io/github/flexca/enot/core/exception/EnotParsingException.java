package io.github.flexca.enot.core.exception;

import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.util.ErrorMessageUtils;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when an eNot template string cannot be parsed.
 *
 * <p>Parsing can fail for several reasons: the input is blank, it is not valid JSON
 * or YAML, a required attribute is missing, an attribute value has the wrong type,
 * or the element body type is incompatible with the parent element's consume type.
 *
 * <p>The list of structured errors is available via {@link #getJsonErrors()}, where
 * each {@link EnotJsonError} carries a JSON-Pointer path to the offending template
 * node and a human-readable description.
 */
public class EnotParsingException extends EnotException {

    private final List<EnotJsonError> jsonErrors;

    public EnotParsingException(String message, List<EnotJsonError> jsonErrors) {
        super(ErrorMessageUtils.compileParsingErrorMessage(message, jsonErrors));
        this.jsonErrors = Collections.unmodifiableList(jsonErrors);
    }

    public EnotParsingException(String message, List<EnotJsonError> jsonErrors, Throwable cause) {
        super(ErrorMessageUtils.compileParsingErrorMessage(message, jsonErrors), cause);
        this.jsonErrors = Collections.unmodifiableList(jsonErrors);
    }

    /**
     * Returns the structured list of parse errors, each with a JSON-Pointer path to the
     * offending template node and a human-readable description.
     *
     * @return an unmodifiable list of errors; never {@code null}
     */
    public List<EnotJsonError> getJsonErrors() {
        return jsonErrors;
    }
}
