package io.github.flexca.enot.core.parser;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.element.value.EnotValueSpecification;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.registry.EnotElementSpecification;
import io.github.flexca.enot.core.registry.EnotTypeSpecification;
import io.github.flexca.enot.core.util.AttributeUtils;
import io.github.flexca.enot.core.util.DateTimeUtils;
import io.github.flexca.enot.core.util.OidUtils;
import io.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EnotParserValidator {

    public void validateElement(EnotTypeSpecification typeSpecification, EnotElement element, String parentPath,
                                List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        EnotElementSpecification elementSpecification = typeSpecification.getElementSpecification(element);
        if (elementSpecification != null) {
            validateAttributes(elementSpecification, element, parentPath, jsonErrors);
            validateBody(elementSpecification.getConsumeType(), element, parentPath, jsonErrors, enotContext);
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
                              List<EnotJsonError> jsonErrors, EnotContext enotContext) {

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
                validateConsumeType(consumeValueSpecification, item, itemPath, jsonErrors, enotContext);
                i++;
            }
        } else {
            validateConsumeType(consumeValueSpecification, objectBody, currentPath, jsonErrors, enotContext);
        }
    }

    private void validateConsumeType(EnotValueSpecification consumeValueSpecification, Object childElementBody, String parentPath,
                                     List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        EnotValueType parentConsumeType = consumeValueSpecification.getType();

        if (!parentConsumeType.haveSuper(CommonEnotValueType.ELEMENT)) {
            if (PlaceholderUtils.isPlaceholder(childElementBody)) {
                // Placeholders can be validated only during serialization:
                return;
            }
        }

        if (childElementBody instanceof EnotElement child) {
            Optional<EnotTypeSpecification> typeSpecification = enotContext.getEnotRegistry().getTypeSpecification(child.getType());
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
                    validateBody(consumeValueSpecification, child, parentPath, jsonErrors, enotContext);
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
