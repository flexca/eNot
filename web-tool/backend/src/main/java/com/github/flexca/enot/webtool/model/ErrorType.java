package com.github.flexca.enot.webtool.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorType {

    GENERIC("generic"), SYNTAX("syntax");

    private final String name;

    ErrorType(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
