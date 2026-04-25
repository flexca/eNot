package io.github.flexca.enot.core.expression.model;

import java.util.List;

public class ExpressionNode extends ExpressionBlock {

    private final List<ExpressionBlock> parts;
    private final Operator operator;

    public ExpressionNode(boolean inverted, List<ExpressionBlock> parts, Operator operator) {
        super(inverted, false);
        this.parts = parts;
        this.operator = operator;
    }

    public List<ExpressionBlock> getParts() {
        return parts;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public ExpressionBlock invert() {
        return new ExpressionNode(!isInverted(), parts, operator);
    }
}
