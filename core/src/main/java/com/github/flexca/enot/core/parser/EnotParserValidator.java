package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.exception.EnotInvalidAttributeException;
import com.github.flexca.enot.core.exception.EnotInvalidBodyException;
import com.github.flexca.enot.core.exception.EnotParsingException;
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
import java.util.Optional;

@RequiredArgsConstructor
public class EnotParserValidator {

    private final EnotRegistry enotRegistry;

    public void validateElement(EnotTypeSpecification typeSpecification, EnotElement element, String parentPath, List<JsonError> jsonErrors) {

        EnotElementSpecification elementSpecification = typeSpecification.getElementSpecification(element);
        if (elementSpecification != null) {
            validateAttributes(elementSpecification, element, parentPath, jsonErrors);
            validateBody(elementSpecification, element, parentPath, jsonErrors);
        }
    }

    private void validateAttributes(EnotElementSpecification elementSpecification, EnotElement element, String parentPath,
                                    List<JsonError> jsonErrors) {

        if (CollectionUtils.isNotEmpty(elementSpecification.getRequiredAttributes())) {
            List<String> missingRequiredAttributes = elementSpecification.getRequiredAttributes().stream()
                    .filter(attribute -> !element.getAttributes().containsKey(attribute))
                    .map(EnotAttribute::getName).toList();
            if (CollectionUtils.isNotEmpty(missingRequiredAttributes)) {
                throw new EnotInvalidAttributeException("missing required attributes for ASN.1 element: " + missingRequiredAttributes);
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

    private void validateBody(EnotElementSpecification elementSpecification, EnotElement element, String parentPath,
                              List<JsonError> jsonErrors) {

        Object objectBody = element.getBody();
        ValueSpecification consumeValueSpecification = elementSpecification.getConsumeType();
        String currentPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;
        if (objectBody instanceof Collection<?> bodyCollection) {
            if (!consumeValueSpecification.isAllowMultipleValues()) {
                jsonErrors.add(JsonError.of(currentPath, "eNot element " + EnotParser.ENOT_ELEMENT_BODY_NAME
                        + " don't allow multiple values"));
            }
            int i = 0;
            for (Object item : bodyCollection) {
                String itemPath = currentPath + "/" + i;
                if (!isValidConsumeType(consumeValueSpecification.getType(), item, itemPath, jsonErrors)) {
                    jsonErrors.add(JsonError.of(itemPath, "eNot element " + EnotParser.ENOT_ELEMENT_BODY_NAME
                            + " type must be of type " + consumeValueSpecification.getType()));
                }
                i++;
            }
        } else {
            if (!isValidConsumeType(consumeValueSpecification.getType(), objectBody, currentPath, jsonErrors)) {
                jsonErrors.add(JsonError.of(currentPath, "eNot element " + EnotParser.ENOT_ELEMENT_BODY_NAME
                        + " type must be of type " + consumeValueSpecification.getType()));
            }
        }
    }

    private boolean isValidConsumeType(ValueType consumeType, Object childElementBody, String parentPath, List<JsonError> jsonErrors) {

        if (ValueType.ELEMENT.equals(consumeType)) {
            return (childElementBody instanceof EnotElement);
        } else {
            if (PlaceholderUtils.isPlaceholder(childElementBody)) {
                // Placeholders can be validated only during runtime:
                return true;
            }

            if (childElementBody instanceof EnotElement child) {
                Optional<EnotTypeSpecification> typeSpecification = enotRegistry.getTypeSpecification(child.getType());
                if (typeSpecification.isEmpty()) {
                    jsonErrors.add(JsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_TYPE_NAME, "unsupported eNot element type"));
                    // Returning true here as we don't want extra invalid consume type error to be added, as root cause
                    // here is missing type specification:
                    return true;
                }

                EnotElementSpecification elementSpecification = typeSpecification.get().getElementSpecification(child);
                if (elementSpecification == null || elementSpecification.getProduceType() == null) {
                    return true;
                }

                ValueSpecification childValueProduceSpecification = elementSpecification.getProduceType();
                return childValueProduceSpecification.getType().equals(consumeType);

            } else {
                if (ValueType.BOOLEAN.equals(consumeType)) {
                    return (childElementBody instanceof Boolean);
                } else if (ValueType.INTEGER.equals(consumeType)) {
                    return (childElementBody instanceof Integer) || (childElementBody instanceof Long)
                            || (childElementBody instanceof BigInteger);
                } else if (ValueType.TEXT.equals(consumeType)) {
                    return (childElementBody instanceof String);
                } else if (ValueType.OBJECT_IDENTIFIER.equals(consumeType)) {
                    return OidUtils.isValidOid(childElementBody);
                } else if (ValueType.DATE_TIME.equals(consumeType)) {
                    return DateTimeUtils.isValidDateTime(childElementBody);
                }
            }
        }
        return false;
    }
}
