package com.github.flexca.enot.core.expression.model;

import java.util.Arrays;
import java.util.List;

public enum Operator {

    EQUALS_OPERATOR(OperatorType.COMPARISON, "=="),
    NOT_EQUALS_OPERATOR(OperatorType.COMPARISON, "!="),
    GREATER_THAN_OPERATOR(OperatorType.COMPARISON, ">"),
    GREATER_THAN_OR_EQUALS_OPERATOR(OperatorType.COMPARISON, ">="),
    LESS_THAN_OPERATOR(OperatorType.COMPARISON, "<"),
    LESS_THAN_OR_EQUALS_OPERATOR(OperatorType.COMPARISON, "<="),

    OR_OPERATOR(OperatorType.BINARY, "||"),
    AND_OPERATOR(OperatorType.BINARY, "&&"),
    NOT_OPERATOR(OperatorType.BINARY, "!");

    private final static List<Operator> COMPARISON_OPERATORS;
    static {
        COMPARISON_OPERATORS = Arrays.stream(values()).filter(value -> OperatorType.COMPARISON.equals(value.type)).toList();
    }

    private final OperatorType type;
    private final String operator;

    private Operator(OperatorType type, String operator) {
        this.type = type;
        this.operator = operator;
    }

    public OperatorType getType() {
        return type;
    }

    public String getOperator() {
        return operator;
    }

    public static List<Operator> getComparisonOperators() {
        return COMPARISON_OPERATORS;
    }
}
