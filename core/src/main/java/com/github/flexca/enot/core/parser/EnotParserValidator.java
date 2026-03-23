package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.asn1.Asn1Tag;
import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.exception.EnotInvalidAttributeException;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.util.AttributeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

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

            }
        } else {

        }
    }

}
