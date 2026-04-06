package com.github.flexca.enot.core.expression.model;

import java.util.List;

public class ExpressionNode extends ExpressionBlock {

    private final List<ExpressionBlock> parts;
    private final Operator operator;

    public ExpressionNode(boolean inverted, ExpressionFunction expressionFunction,
                             List<ExpressionBlock> parts, Operator operator) {
        super(inverted, false, expressionFunction);
        this.parts = parts;
        this.operator = operator;
    }

    public List<ExpressionBlock> getParts() {
        return parts;
    }

    public Operator getOperator() {
        return operator;
    }
}
