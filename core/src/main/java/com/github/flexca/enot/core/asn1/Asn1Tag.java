package com.github.flexca.enot.core.asn1;

import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.struct.value.ValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public enum Asn1Tag {

    SEQUENCE("sequence",
            new ValueSpecification(ValueType.ELEMENT, true),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    SET("set",
            new ValueSpecification(ValueType.ELEMENT, true),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    OBJECT_IDENTIFIER("object_identifier",
            new ValueSpecification(ValueType.OBJECT_IDENTIFIER, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),
    BOOLEAN("boolean",
            new ValueSpecification(ValueType.BOOLEAN, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    INTEGER("integer",
            new ValueSpecification(ValueType.INTEGER, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    OCTET_STRING("octet_string",
            new ValueSpecification(ValueType.BINARY, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    PRINTABLE_STRING("printable_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    IA5_STRING("ia5_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    VISIBLE_STRING("visible_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    UTF8_STRING("utf8_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    BMP_STRING("bmp_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    BIT_STRING("bit_string",
            new ValueSpecification(ValueType.BINARY, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    GENERALIZED_TIME("generalized_time",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    UTC_TIME("utc_time",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT)),

    TAGGED_OBJECT("tagged_object",
            new ValueSpecification(ValueType.ELEMENT, false),
            new ValueSpecification(ValueType.ELEMENT, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL, Asn1Attribute.IMPLICIT, Asn1Attribute.EXPLICIT));

    private static final Map<String, Asn1Tag> BY_NAME = new HashMap<>();
    static {
        for(Asn1Tag value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    @Getter
    private final ValueSpecification consumeType;
    @Getter
    private final ValueSpecification produceType;
    @Getter
    private final Set<Asn1Attribute> requiredAttributes;
    @Getter
    private final Set<Asn1Attribute> allowedAttributes;

    public String getName() {
        return this.name;
    }

    public static Asn1Tag fromString(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
