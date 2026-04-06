package com.github.flexca.enot.core.expression;

public enum OperatorType {

    COMPARISON("comparison"),
    BINARY("binary");

    private final String name;

    private OperatorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
