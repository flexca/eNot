package com.github.flexca.enot.core.expression.model;

import java.util.List;

public class ExpressionFunction extends ExpressionBlock {

    private final ConditionFunction conditionFunction;
    private final List<ExpressionBlock> arguments;

    public ExpressionFunction(boolean inverted, ConditionFunction conditionFunction, List<ExpressionBlock> arguments) {
        super(inverted, false);
        this.conditionFunction = conditionFunction;
        this.arguments = arguments;
    }

    public ConditionFunction getConditionFunction() {
        return conditionFunction;
    }

    public List<ExpressionBlock> getArguments() {
        return arguments;
    }

    @Override
    public ExpressionBlock invert() {
        return new ExpressionFunction(!isInverted(), conditionFunction, arguments);
    }
}
