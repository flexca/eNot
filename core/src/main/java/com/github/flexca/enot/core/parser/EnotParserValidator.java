package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.EnotValueSpecification;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;
import com.github.flexca.enot.core.util.AttributeUtils;
import com.github.flexca.enot.core.util.DateTimeUtils;
import com.github.flexca.enot.core.util.OidUtils;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EnotParserValidator {

    private final EnotRegistry enotRegistry;

    public EnotParserValidator(EnotRegistry enotRegistry) {
        this.enotRegistry = enotRegistry;
    }

    public void validateElement(EnotTypeSpecification typeSpecification, EnotElement element, String parentPath, List<EnotJsonError> jsonErrors) {

        EnotElementSpecification elementSpecification = typeSpecification.getElementSpecification(element);
        if (elementSpecification != null) {
            validateAttributes(elementSpecification, element, parentPath, jsonErrors);
            validateBody(elementSpecification.getConsumeType(), element, parentPath, jsonErrors);
        }
    }

    private void validateAttributes(EnotElementSpecification elementSpecification, EnotElement element, String parentPath,
                                    List<EnotJsonError> jsonErrors) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        if (CollectionUtils.isNotEmpty(elementSpecification.getRequiredAttributes())) {
            List<String> missingRequiredAttributes = elementSpecification.getRequiredAttributes().stream()
                    .filter(attribute -> !element.getAttributes().containsKey(attribute))
                    .map(EnotAttribute::getName).toList();
            if (CollectionUtils.isNotEmpty(missingRequiredAttributes)) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "missing required attributes for eNot element: " + missingRequiredAttributes));
            }
        }

        if (CollectionUtils.isNotEmpty(elementSpecification.getAllowedAttributes())) {
            List<String> unsupportedAttributes = element.getAttributes().keySet().stream()
                    .filter(attribute -> !elementSpecification.getAllowedAttributes().contains(attribute))
                    .map(EnotAttribute::getName).toList();

            if (CollectionUtils.isNotEmpty(unsupportedAttributes)) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "unsupported attributes for eNot element: " + unsupportedAttributes));
            }
        }

        element.getAttributes().forEach((key, value) -> {
            if (!AttributeUtils.isValidAttributeValue(key, value)) {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + key.getName(), "Invalid value type for attribute, expecting "
                        + key.getValueSpecification().getType().getName()));
            }
        });
    }

    private void validateBody(EnotValueSpecification consumeValueSpecification, EnotElement element, String parentPath,
                              List<EnotJsonError> jsonErrors) {

        Object objectBody = element.getBody();
        String currentPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;
        if (objectBody instanceof Collection<?> bodyCollection) {
            if (!consumeValueSpecification.isAllowMultipleValues()) {
                jsonErrors.add(EnotJsonError.of(currentPath, "eNot element " + EnotParser.ENOT_ELEMENT_BODY_NAME
                        + " don't allow multiple values"));
            }
            int i = 0;
            for (Object item : bodyCollection) {
                String itemPath = currentPath + "/" + i;
                validateConsumeType(consumeValueSpecification, item, itemPath, jsonErrors);
                i++;
            }
        } else {
            validateConsumeType(consumeValueSpecification, objectBody, currentPath, jsonErrors);
        }
    }

    private void validateConsumeType(EnotValueSpecification consumeValueSpecification, Object childElementBody, String parentPath, List<EnotJsonError> jsonErrors) {

        EnotValueType parentConsumeType = consumeValueSpecification.getType();

        if (!parentConsumeType.haveSuper(CommonEnotValueType.ELEMENT)) {
            if (PlaceholderUtils.isPlaceholder(childElementBody)) {
                // Placeholders can be validated only during runtime:
                return;
            }
        }

        if (childElementBody instanceof EnotElement child) {
            Optional<EnotTypeSpecification> typeSpecification = enotRegistry.getTypeSpecification(child.getType());
            if (typeSpecification.isEmpty()) {
                jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_TYPE_NAME, "unsupported eNot element type"));
                return;
            }

            EnotElementSpecification elementSpecification = typeSpecification.get().getElementSpecification(child);
            if (elementSpecification == null) {
                return;
            }

            EnotValueSpecification childValueProduceSpecification = elementSpecification.getProduceType();
            boolean canConsume = parentConsumeType.canConsume(childValueProduceSpecification.getType());
            if(!canConsume) {
                if (CommonEnotValueType.ELEMENT.equals(childValueProduceSpecification.getType())) {
                    validateBody(consumeValueSpecification, child, parentPath, jsonErrors);
                } else {
                    jsonErrors.add(EnotJsonError.of(parentPath, "eNot element " + EnotParser.ENOT_ELEMENT_BODY_NAME
                            + " type must be of type " + parentConsumeType));
                }
            }

        } else {
            if (!canConsumeSimpleType(parentConsumeType, childElementBody)) {
                jsonErrors.add(EnotJsonError.of(parentPath, "eNot element " + EnotParser.ENOT_ELEMENT_BODY_NAME
                        + " type must be of type " + parentConsumeType));
            }
        }
    }

    private boolean canConsumeSimpleType(EnotValueType parentConsumeType, Object childElementBody) {

        if (CommonEnotValueType.BOOLEAN.equals(parentConsumeType)) {
            return (childElementBody instanceof Boolean);
        } else if (CommonEnotValueType.INTEGER.equals(parentConsumeType)) {
            return (childElementBody instanceof Integer) || (childElementBody instanceof Long)
                    || (childElementBody instanceof BigInteger);
        } else if (CommonEnotValueType.TEXT.equals(parentConsumeType)) {
            return (childElementBody instanceof String);
        } else if (CommonEnotValueType.OBJECT_IDENTIFIER.equals(parentConsumeType)) {
            return OidUtils.isValidOid(childElementBody);
        } else if (CommonEnotValueType.DATE_TIME.equals(parentConsumeType)) {
            return DateTimeUtils.isValidDateTime(childElementBody);
        } else {
            return false;
        }
    }
}
