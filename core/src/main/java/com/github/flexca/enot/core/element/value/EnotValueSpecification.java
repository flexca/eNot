package com.github.flexca.enot.core.element.value;

public class EnotValueSpecification {

    private final EnotValueType type;
    private final boolean allowMultipleValues;

    public EnotValueSpecification(EnotValueType type, boolean allowMultipleValues) {
        this.type = type;
        this.allowMultipleValues = allowMultipleValues;
    }

    public EnotValueType getType() {
        return type;
    }

    public boolean isAllowMultipleValues() {
        return allowMultipleValues;
    }
}
