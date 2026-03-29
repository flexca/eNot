package com.github.flexca.enot.core.system;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.EnotValueSpecification;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.system.attribute.SystemAttribute;

import java.util.*;

public enum SystemKind implements EnotElementSpecification {

    LOOP("loop",
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS_NAME),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS_NAME, SystemAttribute.MIN_ITEMS, SystemAttribute.MAX_ITEMS)),

    REFERENCE("reference",
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND),
            Set.of(SystemAttribute.KIND)),

    BIT_MAP("bit_map",
            new EnotValueSpecification(CommonEnotValueType.BOOLEAN, true),
            new EnotValueSpecification(CommonEnotValueType.BINARY, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.BYTE_ORDER, SystemAttribute.BIT_ORDER),
            Set.of(SystemAttribute.KIND, SystemAttribute.BYTE_ORDER, SystemAttribute.BIT_ORDER));

    private static final Map<String, SystemKind> BY_NAME = new HashMap<>();
    static {
        for(SystemKind value : values()) {
            BY_NAME.put(value.getName().toLowerCase(Locale.ROOT), value);
        }
    }

    private final String name;
    private final EnotValueSpecification consumeType;
    private final EnotValueSpecification produceType;
    private final Set<EnotAttribute> requiredAttributes;
    private final Set<EnotAttribute> allowedAttributes;

    private SystemKind(String name, EnotValueSpecification consumeType, EnotValueSpecification produceType,
                       Set<EnotAttribute> requiredAttributes, Set<EnotAttribute> allowedAttributes) {
        this.name = name;
        this.consumeType = consumeType;
        this.produceType = produceType;
        this.requiredAttributes = Collections.unmodifiableSet(requiredAttributes);
        this.allowedAttributes = Collections.unmodifiableSet(allowedAttributes);
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

    @JsonCreator
    public static SystemKind fromString(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }

    @Override
    public EnotValueSpecification getConsumeType() {
        return consumeType;
    }

    @Override
    public EnotValueSpecification getProduceType() {
        return produceType;
    }

    @Override
    public Set<EnotAttribute> getRequiredAttributes() {
        return requiredAttributes;
    }

    @Override
    public Set<EnotAttribute> getAllowedAttributes() {
        return allowedAttributes;
    }
}
