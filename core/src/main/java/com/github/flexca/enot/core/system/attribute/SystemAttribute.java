package com.github.flexca.enot.core.system.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueSpecification;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum SystemAttribute implements EnotAttribute {

    KIND("kind", new EnotValueSpecification(CommonEnotValueType.TEXT, false)),
    ITEMS_NAME("items_name", new EnotValueSpecification(CommonEnotValueType.TEXT, false)),
    MIN_ITEMS("min_items", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    MAX_ITEMS("max_items", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    BYTE_ORDER("byte_order", new EnotValueSpecification(CommonEnotValueType.TEXT, false)),
    BIT_ORDER("bit_order", new EnotValueSpecification(CommonEnotValueType.TEXT, false));

    private static final Map<String, SystemAttribute> BY_NAME = new HashMap<>();
    static {
        for(SystemAttribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;
    private final EnotValueSpecification valueSpecification;

    private SystemAttribute(String name, EnotValueSpecification valueSpecification) {
        this.name = name;
        this.valueSpecification = valueSpecification;
    }

    @Override
    public SystemAttribute fromName(String name) {
        return fromJsonString(name);
    }

    @Override
    @JsonValue
    public String getName() {
        return this.name;
    }

    @Override
    public EnotValueSpecification getValueSpecification() {
        return valueSpecification;
    }

    @JsonCreator
    public static SystemAttribute fromJsonString(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }
}
