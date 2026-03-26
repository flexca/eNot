package com.github.flexca.enot.core.exception;

import com.github.flexca.enot.core.parser.JsonError;
import lombok.Getter;

import java.util.List;

public class EnotParsingException extends EnotException {

    @Getter
    private final List<JsonError> jsonErrors;

    public EnotParsingException(String message, List<JsonError> jsonErrors) {
        super(message);
        this.jsonErrors = jsonErrors;
    }

    public EnotParsingException(String message, List<JsonError> jsonErrors, Throwable cause) {
        super(message, cause);
        this.jsonErrors = jsonErrors;
    }
}
