package io.github.flexca.enot.core.element.value.converter;

import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

public class UnsupportedToBinaryConverter implements EnotBinaryConverter {

    @Override
    public byte[] toBinary(Object input) {
        throw new EnotInvalidArgumentException("unsupported conversion to binary");
    }
}
