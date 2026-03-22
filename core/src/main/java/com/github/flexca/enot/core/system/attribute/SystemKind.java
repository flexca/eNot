package com.github.flexca.enot.core.system.attribute;

import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.struct.value.ValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public enum SystemKind {

    LOOP("loop",
            new ValueSpecification(ValueType.ELEMENT, true),
            new ValueSpecification(ValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS));

    private final String name;

    @Getter
    private final ValueSpecification consumeType;
    @Getter
    private final ValueSpecification produceType;
    @Getter
    private final Set<SystemAttribute> requiredAttributes;
    @Getter
    private final Set<SystemAttribute> allowedAttributes;
}
