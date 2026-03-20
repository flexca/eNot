package com.github.flexca.enot.core.struct.attribute;

import com.github.flexca.enot.core.struct.value.ValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommonEnotAttribute implements EnotAttribute {

    TAG("tag", ValueType.TEXT),
    OPTIONAL("optional", ValueType.BOOLEAN);

    @Getter
    private final String name;

    @Getter
    private final ValueType valueType;
}
