package io.github.flexca.enot.core.types.asn1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.github.flexca.enot.core.registry.EnotElementBodyResolver;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.serializer.ElementSerializer;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.registry.EnotElementSpecification;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1BitStringSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1BmpStringSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1BooleanSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1GeneralizedTimeSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1Ia5StringSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1IntegerSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1NullSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1ObjectIdentifierSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1OctetStringSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1PrintableStringSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1SequenceSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1SetSerialized;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1TaggedObjectSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1UtcTimeSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1Utf8StringSerializer;
import io.github.flexca.enot.core.types.asn1.serializer.Asn1VisibleStringSerializer;
import io.github.flexca.enot.core.types.asn1.validation.Asn1ObjectIdentifierValidator;
import io.github.flexca.enot.core.types.asn1.validation.Asn1OctetStringValidator;
import io.github.flexca.enot.core.types.asn1.validation.Asn1TaggedObjectValidator;

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
            new Asn1SequenceSerializer(),
            null),

    SET("set",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, true),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1SetSerialized(),
            null),

    OBJECT_IDENTIFIER("object_identifier",
            new EnotValueSpecification(CommonEnotValueType.OBJECT_IDENTIFIER, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.ALLOWED_VALUES),
            null,
            new Asn1ObjectIdentifierSerializer(),
            new Asn1ObjectIdentifierValidator()),

    BOOLEAN("boolean",
            new EnotValueSpecification(CommonEnotValueType.BOOLEAN, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1BooleanSerializer(),
            null),

    INTEGER("integer",
            new EnotValueSpecification(CommonEnotValueType.INTEGER, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1IntegerSerializer(),
            null),

    OCTET_STRING("octet_string",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.MIN_LENGTH, Asn1Attribute.MAX_LENGTH),
            null,
            new Asn1OctetStringSerializer(),
            new Asn1OctetStringValidator()),

    PRINTABLE_STRING("printable_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.MIN_LENGTH, Asn1Attribute.MAX_LENGTH, Asn1Attribute.ALLOWED_VALUES),
            null,
            new Asn1PrintableStringSerializer(),
            null),

    IA5_STRING("ia5_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.MIN_LENGTH, Asn1Attribute.MAX_LENGTH, Asn1Attribute.ALLOWED_VALUES),
            null,
            new Asn1Ia5StringSerializer(),
            null),

    VISIBLE_STRING("visible_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.MIN_LENGTH, Asn1Attribute.MAX_LENGTH, Asn1Attribute.ALLOWED_VALUES),
            null,
            new Asn1VisibleStringSerializer(),
            null),

    UTF8_STRING("utf8_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.MIN_LENGTH, Asn1Attribute.MAX_LENGTH, Asn1Attribute.ALLOWED_VALUES),
            null,
            new Asn1Utf8StringSerializer(),
            null),

    BMP_STRING("bmp_string",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.MIN_LENGTH, Asn1Attribute.MAX_LENGTH, Asn1Attribute.ALLOWED_VALUES),
            null,
            new Asn1BmpStringSerializer(),
            null),

    BIT_STRING("bit_string",
            new EnotValueSpecification(CommonEnotValueType.BINARY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.APPLY_PADDING),
            null,
            new Asn1BitStringSerializer(),
            null),

    GENERALIZED_TIME("generalized_time",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1GeneralizedTimeSerializer(),
            null),

    UTC_TIME("utc_time",
            new EnotValueSpecification(CommonEnotValueType.TEXT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1UtcTimeSerializer(),
            null),

    NULL("null",
            new EnotValueSpecification(CommonEnotValueType.EMPTY, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG),
            null,
            new Asn1NullSerializer(),
            null),

    TAGGED_OBJECT("tagged_object",
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            new EnotValueSpecification(Asn1EnotValueType.ASN1_ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT),
            null,
            new Asn1TaggedObjectSerializer(),
            new Asn1TaggedObjectValidator());

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
    private final EnotElementValidator specificElementValidator;

    private Asn1Tag(String name, EnotValueSpecification consumeType, EnotValueSpecification produceType,
                    Set<EnotAttribute> requiredAttributes, Set<EnotAttribute> allowedAttributes,
                    EnotElementBodyResolver bodyResolver, ElementSerializer elementSerializer,
                    EnotElementValidator specificElementValidator) {
        this.name = name;
        this.consumeType = consumeType;
        this.produceType = produceType;
        this.requiredAttributes = Collections.unmodifiableSet(requiredAttributes);
        this.allowedAttributes = Collections.unmodifiableSet(allowedAttributes);
        this.bodyResolver = bodyResolver;
        this.elementSerializer = elementSerializer;
        this.specificElementValidator = specificElementValidator;
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

    public EnotElementValidator getSpecificElementValidator() {
        return specificElementValidator;
    }
}
