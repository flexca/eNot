package com.github.flexca.enot.core.types.system;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.element.value.EnotValueSpecification;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.serializer.ElementSerializer;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import com.github.flexca.enot.core.types.system.serializer.SystemConditionSerializer;
import com.github.flexca.enot.core.types.system.serializer.SystemGroupSerializer;
import com.github.flexca.enot.core.types.system.serializer.SystemLoopSerializer;

import java.util.*;

public enum SystemKind implements EnotElementSpecification {

    LOOP("loop",
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS_NAME),
            Set.of(SystemAttribute.KIND, SystemAttribute.ITEMS_NAME, SystemAttribute.MIN_ITEMS, SystemAttribute.MAX_ITEMS),
            null,
            new SystemLoopSerializer()),

    CONDITION("condition",
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.EXPRESSION),
            Set.of(SystemAttribute.KIND, SystemAttribute.EXPRESSION),
            null,
            new SystemConditionSerializer()),

    GROUP("group",
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.GROUP_NAME),
            Set.of(SystemAttribute.KIND, SystemAttribute.GROUP_NAME),
            null,
            new SystemGroupSerializer()),

    REFERENCE("reference",
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            new EnotValueSpecification(CommonEnotValueType.ELEMENT, true),
            Set.of(SystemAttribute.KIND, SystemAttribute.REFERENCE_TYPE, SystemAttribute.REFERENCE_IDENTIFIER),
            Set.of(SystemAttribute.KIND, SystemAttribute.REFERENCE_TYPE, SystemAttribute.REFERENCE_IDENTIFIER),
            null,
            null),

    BIT_MAP("bit_map",
            new EnotValueSpecification(CommonEnotValueType.BOOLEAN, true),
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            Set.of(SystemAttribute.KIND, SystemAttribute.BYTE_ORDER, SystemAttribute.BIT_ORDER),
            Set.of(SystemAttribute.KIND, SystemAttribute.BYTE_ORDER, SystemAttribute.BIT_ORDER),
            null,
            null),

    SHA1("sha1",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            Set.of(SystemAttribute.KIND),
            Set.of(SystemAttribute.KIND),
            null,
            null),

    HEX_TO_BIN("hex_to_bin",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            Set.of(SystemAttribute.KIND),
            Set.of(SystemAttribute.KIND),
            null,
            null),

    BIN_TO_HEX("bin_to_hex",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            Set.of(SystemAttribute.KIND),
            Set.of(SystemAttribute.KIND),
            null,
            null);

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
    private final EnotElementValidator specificElementValidator;
    private final ElementSerializer elementSerializer;

    private SystemKind(String name, EnotValueSpecification consumeType, EnotValueSpecification produceType,
                       Set<EnotAttribute> requiredAttributes, Set<EnotAttribute> allowedAttributes,
                       EnotElementValidator specificElementValidator, ElementSerializer elementSerializer) {
        this.name = name;
        this.consumeType = consumeType;
        this.produceType = produceType;
        this.requiredAttributes = Collections.unmodifiableSet(requiredAttributes);
        this.allowedAttributes = Collections.unmodifiableSet(allowedAttributes);
        this.specificElementValidator = specificElementValidator;
        this.elementSerializer = elementSerializer;
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

    public EnotElementValidator getSpecificElementValidator() {
        return specificElementValidator;
    }

    public ElementSerializer getElementSerializer() {
        return elementSerializer;
    }
}
