package com.github.flexca.enot.core.parser;

import lombok.Getter;

public class JsonError {

    @Getter
    private final String jsonPointer;
    @Getter
    private final String details;

    private JsonError(String jsonPointer, String details) {
        this.jsonPointer = jsonPointer;
        this.details = details;
    }

    public static JsonError of(String jsonPointer, String details) {
        return new JsonError(jsonPointer, details);
    }
}
