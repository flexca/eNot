package com.github.flexca.enot.core.expression.model;

import com.github.flexca.enot.core.element.value.EnotValueType;

public class ExpressionLeaf extends ExpressionBlock {

    private final EnotValueType valueType;
    private final Object value;

    public ExpressionLeaf(boolean inverted, ExpressionFunction expressionFunction,
                             EnotValueType valueType, Object value) {
        super(inverted, true, expressionFunction);
        this.valueType = valueType;
        this.value = value;
    }

    public EnotValueType getValueType() {
        return valueType;
    }

    public Object getValue() {
        return value;
    }
}
