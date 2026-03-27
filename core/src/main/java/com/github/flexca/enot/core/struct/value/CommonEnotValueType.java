package com.github.flexca.enot.core.struct.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Set;

@RequiredArgsConstructor
public enum CommonEnotValueType implements EnotValueType {

    BOOLEAN("boolean", Collections.emptySet()),
    BINARY("binary", Collections.emptySet()),
    INTEGER("integer", Collections.emptySet()),
    TEXT("text", Collections.emptySet()),
    PLACEHOLDER("placeholder", Collections.emptySet()),
    OBJECT_IDENTIFIER("object_identifier", Collections.emptySet()),
    DATE_TIME("date_time", Collections.emptySet()),
    ELEMENT("element", Collections.emptySet()),
    EMPTY("empty", Collections.emptySet());

    @Getter
    private final String name;
    @Getter
    private final Set<EnotValueType> superTypes;
}
