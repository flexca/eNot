package com.github.flexca.enot.core.asn1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.element.value.EnotValueSpecification;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum Asn1Tag implements EnotElementSpecification {

    SEQUENCE("sequence",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, true),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    SET("set",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, true),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    OBJECT_IDENTIFIER("object_identifier",
            new EnotValueSpecification(CommonEnotValueType.OBJECT_IDENTIFIER, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    BOOLEAN("boolean",
            new EnotValueSpecification(CommonEnotValueType.BOOLEAN, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    INTEGER("integer",
            new EnotValueSpecification(CommonEnotValueType.INTEGER, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    OCTET_STRING("octet_string",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    PRINTABLE_STRING("printable_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    IA5_STRING("ia5_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    VISIBLE_STRING("visible_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    UTF8_STRING("utf8_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    BMP_STRING("bmp_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    BIT_STRING("bit_string",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    GENERALIZED_TIME("generalized_time",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    UTC_TIME("utc_time",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    NULL("null",
            new EnotValueSpecification(CommonEnotValueType.EMPTY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    TAGGED_OBJECT("tagged_object",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT));

    private static final Map<String, Asn1Tag> BY_NAME = new HashMap<>();
    static {
        for(Asn1Tag value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;
    private final EnotValueSpecification consumeType;
    private final EnotValueSpecification produceType;
    private final Set<EnotAttribute> requiredAttributes;
    private final Set<EnotAttribute> allowedAttributes;

    private Asn1Tag(String name, EnotValueSpecification consumeType, EnotValueSpecification produceType,
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
    public static Asn1Tag fromString(String name) {
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
