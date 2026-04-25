package io.github.flexca.enot.bertlv.attribute;

import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;

import java.util.HashMap;
import java.util.Map;

public enum BerTlvAttribute implements EnotAttribute {

    TAG("tag", new EnotValueSpecification(CommonEnotValueType.TEXT, false)),
    MIN_LENGTH("min_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    MAX_LENGTH("max_length", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    INDEFINITE_FORM("indefinite_form", new EnotValueSpecification(CommonEnotValueType.BOOLEAN, false));

    private static final Map<String, BerTlvAttribute> BY_NAME = new HashMap<>();
    static {
        for(BerTlvAttribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;
    private final EnotValueSpecification valueSpecification;

    private BerTlvAttribute(String name, EnotValueSpecification valueSpecification) {
        this.name = name;
        this.valueSpecification = valueSpecification;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EnotValueSpecification getValueSpecification() {
        return valueSpecification;
    }

    @Override
    public EnotAttribute fromName(String name) {
        return getByName(name);
    }

    public static EnotAttribute getByName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
