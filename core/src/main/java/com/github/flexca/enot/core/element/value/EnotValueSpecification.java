package com.github.flexca.enot.core.element.value;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnotValueSpecification that = (EnotValueSpecification) o;
        return allowMultipleValues == that.allowMultipleValues && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, allowMultipleValues);
    }

    @Override
    public String toString() {
        return "EnotValueSpecification{" +
                "type=" + type +
                ", allowMultipleValues=" + allowMultipleValues +
                '}';
    }
}
