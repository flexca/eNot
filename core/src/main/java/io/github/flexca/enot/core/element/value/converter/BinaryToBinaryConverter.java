package io.github.flexca.enot.core.element.value.converter;

import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

public class BinaryToBinaryConverter implements EnotBinaryConverter {

    @Override
    public byte[] toBinary(Object input) {

        if (input == null) {
            return null;
        }
        if (input instanceof byte[] binaryInput) {
            return binaryInput;
        } else {
            throw new EnotInvalidArgumentException("expecting input of type " + CommonEnotValueType.BINARY.getName());
        }
    }
}
