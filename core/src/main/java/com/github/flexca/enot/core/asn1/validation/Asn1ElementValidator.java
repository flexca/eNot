package com.github.flexca.enot.core.asn1.validation;

import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.asn1.attribute.Asn1Tag;
import com.github.flexca.enot.core.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.exception.EnotInvalidAttributeException;
import com.github.flexca.enot.core.exception.EnotUnsupportedElementTypeException;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;
import com.github.flexca.enot.core.struct.value.ValueType;
import com.github.flexca.enot.core.util.AttributeUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

public class Asn1ElementValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element) {

        if(!Asn1TypeSpecification.TYPE_NAME.equalsIgnoreCase(element.getType())) {
            throw new EnotUnsupportedElementTypeException("Unsupported element type. Expecting ASN.1");
        }

        Object tagObject = element.getAttributes().get(Asn1Attribute.TAG);
        if (!(tagObject instanceof String)) {
            throw new EnotInvalidAttributeException("For ASN.1 elements tag attribute must be of string type");
        }

        Asn1Tag tag = Asn1Tag.fromString((String) tagObject);

        validateAttributes(tag, element);

        Object objectBody = element.getBody();
        ValueSpecification consumeValueSpecification = tag.getConsumeType();
        if (objectBody instanceof Collection<?> bodyCollection) {
            if (!consumeValueSpecification.isAllowMultipleValues()) {

            }
        } else {

        }
    }

    private void validateAttributes(Asn1Tag tag, EnotElement element) {

        List<String> missingRequiredAttributes = tag.getRequiredAttributes().stream()
                .filter(attribute -> !element.getAttributes().containsKey(attribute))
                .map(EnotAttribute::getName).toList();

        if(CollectionUtils.isNotEmpty(missingRequiredAttributes)) {
            throw new EnotInvalidAttributeException("Missing required attributes for ASN.1 element: " + missingRequiredAttributes);
        }

        List<String> unsupportedAttributes = element.getAttributes().keySet().stream()
                .filter(attribute -> !tag.getAllowedAttributes().contains(attribute))
                .map(EnotAttribute::getName).toList();

        if(CollectionUtils.isNotEmpty(unsupportedAttributes)) {
            throw new EnotInvalidAttributeException("Unsupported attributes for ASN.1 element: " + unsupportedAttributes);
        }

        element.getAttributes().forEach((key, value) -> {
            if (!AttributeUtils.isValidAttributeValue(key, value)) {
                throw new EnotInvalidAttributeException("Invalid value type for attribute " + key.getName() + ", expecting " + key.getValueType().getName());
            }
        });

        if (element.getAttributes().containsKey(Asn1Attribute.EXPLICIT) && element.getAttributes().containsKey(Asn1Attribute.IMPLICIT)) {
            throw new EnotInvalidAttributeException("Only one from implicit or explicit attributes are allowed for ASN.1 element");
        }
    }
}
