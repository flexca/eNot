package io.github.flexca.enot.bertlv;

import io.github.flexca.enot.bertlv.converter.BerTlvElementToBinaryConverter;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

import java.util.Set;

public enum BerTlvValueType implements EnotValueType {

    BER_TLV_ELEMENT("ber_tlv_element", Set.of(CommonEnotValueType.ELEMENT, CommonEnotValueType.BINARY),
            new BerTlvElementToBinaryConverter());

    private final String name;
    private final Set<EnotValueType> superTypes;
    private final EnotBinaryConverter binaryConverter;

    private BerTlvValueType(String name, Set<EnotValueType> superTypes, EnotBinaryConverter binaryConverter) {
        this.name = name;
        this.superTypes = superTypes;
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
        return false;
    }

    @Override
    public EnotBinaryConverter getBinaryConverter() {
        return binaryConverter;
    }
}
