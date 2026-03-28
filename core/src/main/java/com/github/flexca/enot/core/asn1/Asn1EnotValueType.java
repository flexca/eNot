package com.github.flexca.enot.core.asn1;

import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;

import java.util.Collections;
import java.util.Set;

public enum Asn1EnotValueType implements EnotValueType {

    ASN1_ELEMENT("asn1_element", Set.of(CommonEnotValueType.ELEMENT, CommonEnotValueType.BINARY), false);

    private final String name;
    private final Set<EnotValueType> superTypes;
    private final boolean allowedForAttributes;

    private Asn1EnotValueType(String name, Set<EnotValueType> superTypes, boolean allowedForAttributes) {
        this.name = name;
        this.superTypes = Collections.unmodifiableSet(superTypes);
        this.allowedForAttributes = allowedForAttributes;
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
}
