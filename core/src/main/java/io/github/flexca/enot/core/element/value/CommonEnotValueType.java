package io.github.flexca.enot.core.element.value;

import io.github.flexca.enot.core.element.value.converter.BinaryToBinaryConverter;
import io.github.flexca.enot.core.element.value.converter.BooleanToBinaryConverter;
import io.github.flexca.enot.core.element.value.converter.EmptyToBinaryConverter;
import io.github.flexca.enot.core.element.value.converter.IntegerToBinaryConverter;
import io.github.flexca.enot.core.element.value.converter.TextToBinaryConverter;
import io.github.flexca.enot.core.element.value.converter.UnsupportedToBinaryConverter;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

import java.util.Collections;
import java.util.Set;

public enum CommonEnotValueType implements EnotValueType {

    BOOLEAN("boolean", Collections.emptySet(), true, new BooleanToBinaryConverter()),
    BINARY("binary", Collections.emptySet(), false, new BinaryToBinaryConverter()),
    INTEGER("integer", Collections.emptySet(), true, new IntegerToBinaryConverter()),
    TEXT("text", Collections.emptySet(), true, new TextToBinaryConverter()),
    PLACEHOLDER("placeholder", Collections.emptySet(), false, new UnsupportedToBinaryConverter()),
    NULL_VALUE("null", Collections.emptySet(), false, new UnsupportedToBinaryConverter()),
    OBJECT_IDENTIFIER("object_identifier", Set.of(TEXT), false, new UnsupportedToBinaryConverter()),
    DATE_TIME("date_time", Set.of(TEXT), false, new UnsupportedToBinaryConverter()),
    ELEMENT("element", Collections.emptySet(), false, new UnsupportedToBinaryConverter()),
    EMPTY("empty", Collections.emptySet(), false, new EmptyToBinaryConverter());

    private final String name;
    private final Set<EnotValueType> superTypes;
    private final boolean allowedForAttributes;
    private final EnotBinaryConverter binaryConverter;

    private CommonEnotValueType(String name, Set<EnotValueType> superTypes, boolean allowedForAttributes,
                                EnotBinaryConverter binaryConverter) {
        this.name = name;
        this.superTypes = Collections.unmodifiableSet(superTypes);
        this.allowedForAttributes = allowedForAttributes;
        this.binaryConverter = binaryConverter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<EnotValueType> getSuperTypes() {
        return superTypes;
    }

    @Override
    public boolean isAllowedForAttributes() {
        return allowedForAttributes;
    }

    @Override
    public EnotBinaryConverter getBinaryConverter() {
        return binaryConverter;
    }
}
