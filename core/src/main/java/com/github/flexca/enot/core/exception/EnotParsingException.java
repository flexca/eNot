package com.github.flexca.enot.core.exception;

import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.util.ErrorMessageUtils;

import java.util.Collections;
import java.util.List;

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

    public List<EnotJsonError> getJsonErrors() {
        return jsonErrors;
    }
}
