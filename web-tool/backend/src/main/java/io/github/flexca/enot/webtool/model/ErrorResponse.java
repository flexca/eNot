package io.github.flexca.enot.webtool.model;

import io.github.flexca.enot.core.exception.EnotException;
import io.github.flexca.enot.core.exception.EnotParsingException;
import io.github.flexca.enot.core.exception.EnotRuntimeException;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ErrorResponse {

    private final ErrorType errorType;
    private final String errorMessage;
    private final List<EnotJsonError> jsonErrors;

    public static ErrorResponse fromException(EnotParsingException parsingException) {
        return new ErrorResponse(ErrorType.SYNTAX, null, parsingException.getJsonErrors());
    }

    public static ErrorResponse fromException(EnotSerializationException serializationException) {
        return new ErrorResponse(ErrorType.SYNTAX, null, serializationException.getJsonErrors());
    }

    public static ErrorResponse fromException(EnotRuntimeException enotRuntimeException) {
        return new ErrorResponse(ErrorType.GENERIC, enotRuntimeException.getMessage(), null);
    }

    public static ErrorResponse fromException(EnotException enotException) {
        return new ErrorResponse(ErrorType.GENERIC, enotException.getMessage(), null);
    }

    public static ErrorResponse fromException(Exception exception) {
        return new ErrorResponse(ErrorType.GENERIC, "Unexpected error. Check logs for details", null);
    }
}
