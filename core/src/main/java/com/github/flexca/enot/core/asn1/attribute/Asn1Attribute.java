package com.github.flexca.enot.core.asn1.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum Asn1Attribute implements EnotAttribute {

    TAG("tag", CommonEnotValueType.TEXT),
    OPTIONAL("optional", CommonEnotValueType.BOOLEAN),
    IMPLICIT("implicit", CommonEnotValueType.INTEGER),
    EXPLICIT("explicit", CommonEnotValueType.INTEGER);

    private static final Map<String, Asn1Attribute> BY_NAME = new HashMap<>();
    static {
        for(Asn1Attribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    @Getter
    private final EnotValueType valueType;

    @Override
    public Asn1Attribute fromName(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

    @JsonCreator
    public static Asn1Attribute fromJsonString(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }
}
