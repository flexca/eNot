package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.exception.EnotInvalidAttributeException;
import com.github.flexca.enot.core.exception.EnotInvalidBodyException;
import com.github.flexca.enot.core.exception.EnotParseException;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.struct.value.ValueType;
import com.github.flexca.enot.core.util.AttributeUtils;
import com.github.flexca.enot.core.util.DateTimeUtils;
import com.github.flexca.enot.core.util.OidUtils;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class EnotParserValidator {

    private final EnotRegistry enotRegistry;

    public void validateElement(EnotTypeSpecification typeSpecification, EnotElement element) {

        EnotElementSpecification elementSpecification = typeSpecification.getElementSpecification(element);
        if (elementSpecification != null) {
            validateAttributes(elementSpecification, element);
            validateBody(elementSpecification, element);
        }
    }

    private void validateAttributes(EnotElementSpecification elementSpecification, EnotElement element) {

        if (CollectionUtils.isNotEmpty(elementSpecification.getRequiredAttributes())) {
            List<String> missingRequiredAttributes = elementSpecification.getRequiredAttributes().stream()
                    .filter(attribute -> !element.getAttributes().containsKey(attribute))
                    .map(EnotAttribute::getName).toList();
            if (CollectionUtils.isNotEmpty(missingRequiredAttributes)) {
                throw new EnotInvalidAttributeException("Missing required attributes for ASN.1 element: " + missingRequiredAttributes);
            }
        }

        if (CollectionUtils.isNotEmpty(elementSpecification.getAllowedAttributes())) {
            List<String> unsupportedAttributes = element.getAttributes().keySet().stream()
                    .filter(attribute -> !elementSpecification.getAllowedAttributes().contains(attribute))
                    .map(EnotAttribute::getName).toList();

            if (CollectionUtils.isNotEmpty(unsupportedAttributes)) {
                throw new EnotInvalidAttributeException("Unsupported attributes for ASN.1 element: " + unsupportedAttributes);
            }
        }

        element.getAttributes().forEach((key, value) -> {
            if (!AttributeUtils.isValidAttributeValue(key, value)) {
                throw new EnotInvalidAttributeException("Invalid value type for attribute " + key.getName() + ", expecting " + key.getValueType().getName());
            }
        });
    }

    private void validateBody(EnotElementSpecification elementSpecification, EnotElement element) {

        Object objectBody = element.getBody();
        ValueSpecification consumeValueSpecification = elementSpecification.getConsumeType();
        if (objectBody instanceof Collection<?> bodyCollection) {
            if (!consumeValueSpecification.isAllowMultipleValues()) {
                throw new EnotInvalidBodyException("Body of element with type " + element.getType() + " don't allow multiple values");
            }
            for (Object item : bodyCollection) {
                if (!isValidConsumeType(consumeValueSpecification.getType(), item)) {
                    throw new EnotInvalidBodyException("Body of element with type " + element.getType() + " don't allow multiple values");
                }
            }
        } else {
            if (!isValidConsumeType(consumeValueSpecification.getType(), objectBody)) {
                throw new EnotInvalidBodyException("Body of element with type " + element.getType() + " expect " + consumeValueSpecification.getType());
            }
        }
    }

    private boolean isValidConsumeType(ValueType type, Object childElementBody) {

        if (ValueType.ELEMENT.equals(type)) {
            return (childElementBody instanceof EnotElement);
        } else {
            if (PlaceholderUtils.isPlaceholder(childElementBody)) {
                // Placeholders can be validated only during runtime:
                return true;
            }

            if (ValueType.BOOLEAN.equals(type)) {
                if (childElementBody instanceof EnotElement child) {
                    ValueSpecification childValueProduceSpecification = getElementProduceValueSpecification(child);
                    return childValueProduceSpecification.getType().equals(ValueType.BOOLEAN);
                }
                return (childElementBody instanceof Boolean);
            } else if (ValueType.BINARY.equals(type)) {
                if (childElementBody instanceof EnotElement child) {
                    ValueSpecification childValueProduceSpecification = getElementProduceValueSpecification(child);
                    return childValueProduceSpecification.getType().equals(ValueType.BOOLEAN);
                }
            } else if (ValueType.INTEGER.equals(type)) {
                if (childElementBody instanceof EnotElement child) {
                    ValueSpecification childValueProduceSpecification = getElementProduceValueSpecification(child);
                    return childValueProduceSpecification.getType().equals(ValueType.INTEGER);
                }
                return (childElementBody instanceof Integer) || (childElementBody instanceof Long)
                        || (childElementBody instanceof BigInteger);
            } else if (ValueType.TEXT.equals(type)) {
                if (childElementBody instanceof EnotElement child) {
                    ValueSpecification childValueProduceSpecification = getElementProduceValueSpecification(child);
                    return childValueProduceSpecification.getType().equals(ValueType.TEXT);
                }
                return (childElementBody instanceof String);
            } else if (ValueType.OBJECT_IDENTIFIER.equals(type)) {
                if (childElementBody instanceof EnotElement child) {
                    ValueSpecification childValueProduceSpecification = getElementProduceValueSpecification(child);
                    return childValueProduceSpecification.getType().equals(ValueType.OBJECT_IDENTIFIER);
                }
                return OidUtils.isValidOid(childElementBody);
            } else if (ValueType.DATE_TIME.equals(type)) {
                if (childElementBody instanceof EnotElement child) {
                    ValueSpecification childValueProduceSpecification = getElementProduceValueSpecification(child);
                    return childValueProduceSpecification.getType().equals(ValueType.DATE_TIME);
                }
                return DateTimeUtils.isValidDateTime(childElementBody);
            }
        }
        return false;
    }

    private ValueSpecification getElementProduceValueSpecification(EnotElement element) {

        EnotTypeSpecification typeSpecification = enotRegistry.getTypeSpecification(element.getType()).orElseThrow(() ->
                new EnotParseException("Element type not found"));
        return typeSpecification.getElementSpecification(element).getProduceType();
    }
}
