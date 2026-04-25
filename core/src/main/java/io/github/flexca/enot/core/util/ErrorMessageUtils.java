package io.github.flexca.enot.core.util;

import io.github.flexca.enot.core.parser.EnotJsonError;

import java.util.List;

public class ErrorMessageUtils {

    private ErrorMessageUtils() {
    }

    public static String compileParsingErrorMessage(String message, List<EnotJsonError> jsonErrors) {

        StringBuilder builder = new StringBuilder(message);
        for (EnotJsonError jsonError : jsonErrors) {
            builder.append("\n JSON pointer: \"")
                    .append(jsonError.getJsonPointer())
                    .append("\"")
                    .append(", details: ")
                    .append(jsonError.getDetails());
        }
        return builder.toString();
    }
}
