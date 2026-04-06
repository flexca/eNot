package com.github.flexca.enot.core.expression;

public enum ExpressionFunction {

    DATE_TIME("date_time");

    private final String name;

    private ExpressionFunction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
