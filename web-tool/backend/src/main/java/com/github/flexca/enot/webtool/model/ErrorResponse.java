package com.github.flexca.enot.webtool.model;

import com.github.flexca.enot.core.exception.EnotException;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotRuntimeException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import lombok.Data;

@Data
public class ErrorResponse {

    // private List<>

    public static ErrorResponse fromException(EnotParsingException parsingException) {
        return null;
    }

    public static ErrorResponse fromException(EnotSerializationException serializationException) {
        return null;
    }

    public static ErrorResponse fromException(EnotRuntimeException enotRuntimeException) {
        return null;
    }

    public static ErrorResponse fromException(EnotException enotException) {
        return null;
    }

    public static ErrorResponse fromException(Exception exception) {
        return null;
    }
}
