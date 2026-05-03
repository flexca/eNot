package io.github.flexca.enot.bertlv;

import io.github.flexca.enot.bertlv.converter.BerTlvElementToBinaryConverter;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

import java.util.Set;

/**
 * Enumerates the value types produced and consumed by the BER-TLV module.
 * <p>
 * Implements {@link EnotValueType} so that the eNot framework can resolve type
 * compatibility and locate the binary converter for BER-TLV elements.
 */
public enum BerTlvValueType implements EnotValueType {

    /**
     * Represents a fully constructed {@link io.github.flexca.enot.bertlv.model.BerTlvElement}
     * instance that is ready to be encoded to binary.
     * <p>
     * Super-types: {@code ELEMENT} and {@code BINARY}, so the framework treats it as a
     * binary-compatible value and can pass it to parent element serializers.
     * The associated converter is {@link io.github.flexca.enot.bertlv.converter.BerTlvElementToBinaryConverter}.
     */
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
