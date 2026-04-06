package com.github.flexca.enot.core.expression;

public class ComparisonExpression {

    private final String left;
    private final String right;
    private final Operator operator;

    public static ComparisonExpression of(String left, String right, Operator operator) {
        // "date_time(${valid_from}) >= date_time('2050-01-01T00-00-00Z')"
        return new ComparisonExpression(left, right, operator);
    }

    private ComparisonExpression(String left, String right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public Operator getOperator() {
        return operator;
    }
}
