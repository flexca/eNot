package com.github.flexca.enot.core.asn1;

import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public enum Asn1EnotValueType implements EnotValueType {

    ASN1_ELEMENT("asn1_element", Set.of(CommonEnotValueType.ELEMENT, CommonEnotValueType.BINARY));

    @Getter
    private final String name;

    @Getter
    private final Set<EnotValueType> superTypes;

}
