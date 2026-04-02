package com.github.flexca.enot.core.element.value.converter;

import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.registry.EnotBinaryConverter;

public class BooleanToBinaryConverter implements EnotBinaryConverter {

    @Override
    public byte[] toBinary(Object input) {

        if (input == null) {
            return null;
        }
        if (input instanceof Boolean booleanInput) {
            byte[] result = new byte[1];
            result[0] = booleanInput ? (byte) 0x1 : 0x0;
            return result;
        } else {
            throw new EnotInvalidArgumentException("expecting input of type " + CommonEnotValueType.BOOLEAN.getName());
        }
    }
}
