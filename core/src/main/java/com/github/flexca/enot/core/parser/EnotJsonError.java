package com.github.flexca.enot.core.parser;

public class EnotJsonError {

    private final String jsonPointer;
    private final String details;

    public EnotJsonError(String jsonPointer, String details) {
        this.jsonPointer = jsonPointer;
        this.details = details;
    }

    public static EnotJsonError of(String jsonPointer, String details) {
        return new EnotJsonError(jsonPointer, details);
    }

    public String getJsonPointer() {
        return jsonPointer;
    }

    public String getDetails() {
        return details;
    }
}
