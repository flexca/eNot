package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.struct.value.CommonEnotValueType;
import com.github.flexca.enot.core.struct.value.EnotValueType;
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

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        if (CollectionUtils.isNotEmpty(elementSpecification.getRequiredAttributes())) {
            List<String> missingRequiredAttributes = elementSpecification.getRequiredAttributes().stream()
                    .filter(attribute -> !element.getAttributes().containsKey(attribute))
                    .map(EnotAttribute::getName).toList();
            if (CollectionUtils.isNotEmpty(missingRequiredAttributes)) {
                jsonErrors.add(JsonError.of(attributesPath, "missing required attributes for ASN.1 element: " + missingRequiredAttributes));
            }
        }

        if (CollectionUtils.isNotEmpty(elementSpecification.getAllowedAttributes())) {
            List<String> unsupportedAttributes = element.getAttributes().keySet().stream()
                    .filter(attribute -> !elementSpecification.getAllowedAttributes().contains(attribute))
                    .map(EnotAttribute::getName).toList();

            if (CollectionUtils.isNotEmpty(unsupportedAttributes)) {
                jsonErrors.add(JsonError.of(attributesPath, "unsupported attributes for ASN.1 element: " + unsupportedAttributes));
            }
        }

        element.getAttributes().forEach((key, value) -> {
            if (!AttributeUtils.isValidAttributeValue(key, value)) {
                jsonErrors.add(JsonError.of(attributesPath + "/" + key.getName(), "Invalid value type for attribute, expecting "
                        + key.getValueType().getName()));
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
                validateConsumeType(consumeValueSpecification.getType(), item, itemPath, jsonErrors);
                i++;
            }
        } else {
            validateConsumeType(consumeValueSpecification.getType(), objectBody, currentPath, jsonErrors);
        }
    }

    private void validateConsumeType(EnotValueType parentConsumeType, Object childElementBody, String parentPath, List<JsonError> jsonErrors) {

        if (!parentConsumeType.haveSuper(CommonEnotValueType.ELEMENT)) {
            if (PlaceholderUtils.isPlaceholder(childElementBody)) {
                // Placeholders can be validated only during runtime:
                return;
            }
        }

        if (childElementBody instanceof EnotElement child) {
            Optional<EnotTypeSpecification> typeSpecification = enotRegistry.getTypeSpecification(child.getType());
            if (typeSpecification.isEmpty()) {
                jsonErrors.add(JsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_TYPE_NAME, "unsupported eNot element type"));
                return;
            }

            EnotElementSpecification elementSpecification = typeSpecification.get().getElementSpecification(child);
            if (elementSpecification == null) {
                return;
            }

            ValueSpecification childValueProduceSpecification = elementSpecification.getProduceType();
            boolean canConsume = parentConsumeType.canConsume(childValueProduceSpecification.getType());
            if(!canConsume && CommonEnotValueType.ELEMENT.equals(childValueProduceSpecification.getType())) {
                validateBody()
            } else {
                jsonErrors.add(JsonError.of(parentPath, "eNot element " + EnotParser.ENOT_ELEMENT_BODY_NAME
                        + " type must be of type " + parentConsumeType));
            }

        } else {
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
            }
        }

        return false;
    }
}
