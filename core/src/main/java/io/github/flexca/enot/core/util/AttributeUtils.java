package io.github.flexca.enot.core.util;

import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueType;

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
