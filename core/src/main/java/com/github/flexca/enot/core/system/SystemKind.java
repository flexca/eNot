package com.github.flexca.enot.core.system;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.asn1.Asn1Tag;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.struct.value.ValueType;
import com.github.flexca.enot.core.system.attribute.SystemAttribute;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public enum SystemKind implements EnotElementSpecification {

    LOOP("loop",
            new ValueSpecification(ValueType.ELEMENT, true),
            new ValueSpecification(ValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS));

    private static final Map<String, SystemKind> BY_NAME = new HashMap<>();
    static {
        for(SystemKind value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    @Getter
    private final ValueSpecification consumeType;
    @Getter
    private final ValueSpecification produceType;
    @Getter
    private final Set<SystemAttribute> requiredAttributes;
    @Getter
    private final Set<SystemAttribute> allowedAttributes;

    @JsonValue
    public String getName() {
        return this.name;
    }

    @JsonCreator
    public static SystemKind fromString(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
