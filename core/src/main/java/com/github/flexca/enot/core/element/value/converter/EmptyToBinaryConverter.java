package com.github.flexca.enot.core.element.value.converter;

import com.github.flexca.enot.core.registry.EnotBinaryConverter;

public class EmptyToBinaryConverter implements EnotBinaryConverter {

    @Override
    public byte[] toBinary(Object input) {
        return null;
    }
}
