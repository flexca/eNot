package io.github.flexca.enot.core.exception;

import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.util.ErrorMessageUtils;

import java.util.Collections;
import java.util.List;

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

    public List<EnotJsonError> getJsonErrors() {
        return jsonErrors;
    }
}
