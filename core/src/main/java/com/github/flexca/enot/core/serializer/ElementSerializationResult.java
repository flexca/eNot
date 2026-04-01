package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.element.value.EnotValueType;

public class ElementSerializationResult {

    private final EnotValueType valueType;
    private final Object data;

    public static ElementSerializationResult of(EnotValueType valueType, Object data) {
        return new ElementSerializationResult(valueType, data);
    }

    private ElementSerializationResult(EnotValueType valueType, Object data) {
        this.valueType = valueType;
        this.data = data;
    }

    public EnotValueType getValueType() {
        return valueType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }
}
