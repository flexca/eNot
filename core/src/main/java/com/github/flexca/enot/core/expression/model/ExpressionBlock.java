package com.github.flexca.enot.core.expression.model;

public abstract class ExpressionBlock {

    private final boolean inverted;
    private final boolean leaf;
    private final ExpressionFunction expressionFunction;

    protected ExpressionBlock(boolean inverted, boolean leaf, ExpressionFunction expressionFunction) {
        this.inverted = inverted;
        this.leaf = leaf;
        this.expressionFunction = expressionFunction;
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public ExpressionFunction getExpressionFunction() {
        return expressionFunction;
    }
}
