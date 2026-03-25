package com.github.flexca.enot.core.asn1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.struct.value.ValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.ASN1Null;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public enum Asn1Tag implements EnotElementSpecification {

    SEQUENCE("sequence",
            new ValueSpecification(ValueType.BINARY, true),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    SET("set",
            new ValueSpecification(ValueType.BINARY, true),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    OBJECT_IDENTIFIER("object_identifier",
            new ValueSpecification(ValueType.OBJECT_IDENTIFIER, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    BOOLEAN("boolean",
            new ValueSpecification(ValueType.BOOLEAN, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    INTEGER("integer",
            new ValueSpecification(ValueType.INTEGER, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    OCTET_STRING("octet_string",
            new ValueSpecification(ValueType.BINARY, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    PRINTABLE_STRING("printable_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    IA5_STRING("ia5_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    VISIBLE_STRING("visible_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    UTF8_STRING("utf8_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    BMP_STRING("bmp_string",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    BIT_STRING("bit_string",
            new ValueSpecification(ValueType.BINARY, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    GENERALIZED_TIME("generalized_time",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    UTC_TIME("utc_time",
            new ValueSpecification(ValueType.TEXT, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG, Asn1Attribute.OPTIONAL)),

    NULL("null",
            new ValueSpecification(ValueType.EMPTY, false),
            new ValueSpecification(ValueType.BINARY, false),
            Set.of(Asn1Attribute.TAG),
            Set.of(Asn1Attribute.TAG)),

    TAGGED_OBJECT("tagged_object",
            new ValueSpecification(ValueType.BINARY, false),
            new ValueSpecification(ValueType.BINARY, false),
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
    private final Set<EnotAttribute> requiredAttributes;
    @Getter
    private final Set<EnotAttribute> allowedAttributes;

    @JsonValue
    public String getName() {
        return this.name;
    }

    @JsonCreator
    public static Asn1Tag fromString(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
