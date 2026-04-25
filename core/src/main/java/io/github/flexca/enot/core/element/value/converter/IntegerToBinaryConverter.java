package io.github.flexca.enot.core.element.value.converter;

import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;

import java.math.BigInteger;

public class IntegerToBinaryConverter implements EnotBinaryConverter {

    @Override
    public byte[] toBinary(Object input) {

        if (input == null) {
            return null;
        }
        if (input instanceof Integer integerInput) {
            return BigInteger.valueOf(integerInput).toByteArray();
        } else if (input instanceof Long longInput) {
            return BigInteger.valueOf(longInput).toByteArray();
        } else if (input instanceof BigInteger bigIntegerInput) {
            return bigIntegerInput.toByteArray();
        } else {
            throw new EnotInvalidArgumentException("expecting input of type " + CommonEnotValueType.INTEGER.getName());
        }
    }
}
