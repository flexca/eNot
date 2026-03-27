package com.github.flexca.enot.core.util;

import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;

import java.math.BigInteger;

public class AttributeUtils {

    private AttributeUtils() {
    }

    public static boolean isValidAttributeValue(EnotAttribute attribute, Object value) {

        if (CommonEnotValueType.BOOLEAN.equals(attribute.getValueType())) {
            return (value instanceof Boolean);
        } else if (CommonEnotValueType.INTEGER.equals(attribute.getValueType())) {
            return (value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger);
        } else if (CommonEnotValueType.TEXT.equals(attribute.getValueType())) {
            return (value instanceof String);
        }
        return false;
    }
}
