package com.github.flexca.enot.core.asn1.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueSpecification;
import com.github.flexca.enot.core.struct.value.EnotValueType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum Asn1Attribute implements EnotAttribute {

    TAG("tag", new EnotValueSpecification(CommonEnotValueType.TEXT, false)),
    OPTIONAL("optional", new EnotValueSpecification(CommonEnotValueType.BOOLEAN, false)),
    IMPLICIT("implicit", new EnotValueSpecification(CommonEnotValueType.INTEGER, false)),
    EXPLICIT("explicit", new EnotValueSpecification(CommonEnotValueType.INTEGER, false));

    private static final Map<String, Asn1Attribute> BY_NAME = new HashMap<>();
    static {
        for(Asn1Attribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;
    private final EnotValueSpecification ValueSpecification;

    private Asn1Attribute(String name, EnotValueSpecification ValueSpecification) {
        this.name = name;
        this.ValueSpecification = ValueSpecification;
    }

    @Override
    public Asn1Attribute fromName(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }

    @Override
    @JsonValue
    public String getName() {
        return this.name;
    }

    @Override
    public EnotValueSpecification getValueSpecification() {
        return ValueSpecification;
    }

    @JsonCreator
    public static Asn1Attribute fromJsonString(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }
}
