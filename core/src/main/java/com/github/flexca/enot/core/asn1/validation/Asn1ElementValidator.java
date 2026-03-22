package com.github.flexca.enot.core.asn1.validation;

import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.asn1.attribute.Asn1Tag;
import com.github.flexca.enot.core.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.exception.EnotInvalidAttributeException;
import com.github.flexca.enot.core.exception.EnotUnsupportedElementTypeException;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class Asn1ElementValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element) {

        if(!Asn1TypeSpecification.TYPE_NAME.equalsIgnoreCase(element.getType())) {
            throw new EnotUnsupportedElementTypeException("Unsupported element type. Expecting ASN.1");
        }

        Object tagObject = element.getAttributes().get(Asn1Attribute.TAG);
        if (tagObject instanceof String tagString) {
            Asn1Tag tag = Asn1Tag.fromString(tagString);
            validateAttributes(tag, element);
        } else {
            throw new EnotInvalidAttributeException("For ASN.1 elements tag attribute must be of string type");
        }
    }

    private void validateAttributes(Asn1Tag tag, EnotElement element) {

        List<String> missingRequiredAttributes = tag.getRequiredAttributes().stream()
                .filter(attribute -> !element.getAttributes().keySet().contains(attribute))
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
    }
}
