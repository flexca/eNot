package io.github.flexca.enot.core.parser;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnotJsonError that = (EnotJsonError) o;
        return Objects.equals(jsonPointer, that.jsonPointer) && Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonPointer, details);
    }

    @Override
    public String toString() {
        return "EnotJsonError{" +
                "jsonPointer='" + jsonPointer + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
