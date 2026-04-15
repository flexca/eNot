package com.github.flexca.enot.core.types.asn1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.registry.EnotElementBodyResolver;
import com.github.flexca.enot.core.serializer.ElementSerializer;
import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.element.value.EnotValueSpecification;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1BitStringSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1BmpStringSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1BooleanSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1GeneralizedTimeSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1Ia5StringSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1IntegerSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1NullSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1ObjectIdentifierSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1OctetStringSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1PrintableStringSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1SequenceSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1SetSerialized;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1TaggedObjectSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1UtcTimeSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1Utf8StringSerializer;
import com.github.flexca.enot.core.types.asn1.serializer.Asn1VisibleStringSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum Asn1Tag implements EnotElementSpecification {

    SEQUENCE("sequence",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, true),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1SequenceSerializer()),

    SET("set",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, true),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1SetSerialized()),

    OBJECT_IDENTIFIER("object_identifier",
            new EnotValueSpecification(CommonEnotValueType.OBJECT_IDENTIFIER, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1ObjectIdentifierSerializer()),

    BOOLEAN("boolean",
            new EnotValueSpecification(CommonEnotValueType.BOOLEAN, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1BooleanSerializer()),

    INTEGER("integer",
            new EnotValueSpecification(CommonEnotValueType.INTEGER, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1IntegerSerializer()),

    OCTET_STRING("octet_string",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1OctetStringSerializer()),

    PRINTABLE_STRING("printable_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1PrintableStringSerializer()),

    IA5_STRING("ia5_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1Ia5StringSerializer()),

    VISIBLE_STRING("visible_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1VisibleStringSerializer()),

    UTF8_STRING("utf8_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1Utf8StringSerializer()),

    BMP_STRING("bmp_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1BmpStringSerializer()),

    BIT_STRING("bit_string",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.APPLY_PADDING),
            null,
            new Asn1BitStringSerializer()),

    GENERALIZED_TIME("generalized_time",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1GeneralizedTimeSerializer()),

    UTC_TIME("utc_time",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1UtcTimeSerializer()),

    NULL("null",
            new EnotValueSpecification(CommonEnotValueType.EMPTY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1NullSerializer()),

    TAGGED_OBJECT("tagged_object",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT),
            null,
            new Asn1TaggedObjectSerializer());

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
    private final EnotElementBodyResolver bodyResolver;
    private final ElementSerializer elementSerializer;

    private Asn1Tag(String name, EnotValueSpecification consumeType, EnotValueSpecification produceType,
                    Set<EnotAttribute> requiredAttributes, Set<EnotAttribute> allowedAttributes,
                    EnotElementBodyResolver bodyResolver, ElementSerializer elementSerializer) {
        this.name = name;
        this.consumeType = consumeType;
        this.produceType = produceType;
        this.requiredAttributes = Collections.unmodifiableSet(requiredAttributes);
        this.allowedAttributes = Collections.unmodifiableSet(allowedAttributes);
        this.bodyResolver = bodyResolver;
        this.elementSerializer = elementSerializer;
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

    @Override
    public EnotElementBodyResolver getBodyResolver() {
        return bodyResolver;
    }

    public ElementSerializer getElementSerializer() {
        return elementSerializer;
    }
}
