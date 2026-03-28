package com.github.flexca.enot.core.util;

import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;

import java.math.BigInteger;

public class AttributeUtils {

    private AttributeUtils() {
    }

    public static boolean isValidAttributeValue(EnotAttribute attribute, Object value) {

        EnotValueType expectedType = attribute.getValueSpecification().getType();
        if (CommonEnotValueType.BOOLEAN.equals(expectedType)) {
            return (value instanceof Boolean);
        } else if (CommonEnotValueType.INTEGER.equals(expectedType)) {
            return (value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger);
        } else if (CommonEnotValueType.TEXT.equals(expectedType)) {
            return (value instanceof String);
        }
        return false;
    }
}
