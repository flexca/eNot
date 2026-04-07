package com.github.flexca.enot.core.expression.model;

import com.github.flexca.enot.core.element.value.EnotValueType;

public class ExpressionLeaf extends ExpressionBlock {

    private final EnotValueType valueType;
    private final Object value;

    public ExpressionLeaf(boolean inverted, EnotValueType valueType, Object value) {
        super(inverted, true);
        this.valueType = valueType;
        this.value = value;
    }

    public EnotValueType getValueType() {
        return valueType;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public ExpressionBlock invert() {
        return new ExpressionLeaf(!isInverted(), valueType, value);
    }
}
