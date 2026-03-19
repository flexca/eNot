package com.github.flexca.enot.core.struct.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValueType {

    BOOLEAN("boolean"),
    BINARY("binary"),
    INTEGER("integer"),
    TEXT("text"),
    PLACEHOLDER("placeholder"),
    OBJECT_IDENTIFIER("object_identifier"),
    DATE_TIME("date_time"),
    ELEMENT("element");

    @Getter
    private final String name;
}
