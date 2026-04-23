package com.github.flexca.enot.webtool.error;

import com.github.flexca.enot.core.exception.EnotException;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotRuntimeException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.webtool.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class EnotResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { EnotParsingException.class })
    protected ResponseEntity<Object> handleEnotParsingException(EnotParsingException parsingException, WebRequest request) {
        log.error("Error: {}", parsingException.getMessage(), parsingException);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return handleExceptionInternal(parsingException, ErrorResponse.fromException(parsingException), headers,
                HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { EnotSerializationException.class })
    protected ResponseEntity<Object> handleEnotSerializationException(EnotSerializationException serializationException,
                                                                      WebRequest request) {
        log.error("Error: {}", serializationException.getMessage(), serializationException);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return handleExceptionInternal(serializationException, ErrorResponse.fromException(serializationException), headers,
                HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { EnotRuntimeException.class })
    protected ResponseEntity<Object> handleEnotRuntimeException(EnotRuntimeException enotRuntimeException, WebRequest request) {
        log.error("Error: {}", enotRuntimeException.getMessage(), enotRuntimeException);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return handleExceptionInternal(enotRuntimeException, ErrorResponse.fromException(enotRuntimeException), headers,
                HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { EnotException.class })
    protected ResponseEntity<Object> handleEnotException(EnotException enotException, WebRequest request) {
        log.error("Error: {}", enotException.getMessage(), enotException);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return handleExceptionInternal(enotException, ErrorResponse.fromException(enotException), headers,
                HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> handleGenericException(Exception exception, WebRequest request) {
        log.error("Error: {}", exception.getMessage(), exception);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return handleExceptionInternal(exception, ErrorResponse.fromException(exception), headers,
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
