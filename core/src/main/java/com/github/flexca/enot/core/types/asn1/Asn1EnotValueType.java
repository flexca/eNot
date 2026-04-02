package com.github.flexca.enot.core.types.asn1;

import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.element.value.EnotValueType;
import com.github.flexca.enot.core.registry.EnotBinaryConverter;
import com.github.flexca.enot.core.types.asn1.converter.Asn1ElementToBinaryConverter;

import java.util.Collections;
import java.util.Set;

public enum Asn1EnotValueType implements EnotValueType {

    ASN1_ELEMENT("asn1_element", Set.of(CommonEnotValueType.ELEMENT, CommonEnotValueType.BINARY),
            false, new Asn1ElementToBinaryConverter());

    private final String name;
    private final Set<EnotValueType> superTypes;
    private final boolean allowedForAttributes;
    private final EnotBinaryConverter binaryConverter;

    private Asn1EnotValueType(String name, Set<EnotValueType> superTypes, boolean allowedForAttributes,
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
