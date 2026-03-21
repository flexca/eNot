package com.github.flexca.enot.core.asn1.validation;

import com.github.flexca.enot.core.asn1.Asn1Attribute;
import com.github.flexca.enot.core.asn1.Asn1Tag;
import com.github.flexca.enot.core.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.exception.EnotInvalidAttributeException;
import com.github.flexca.enot.core.exception.EnotUnsupportedElementTypeException;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.struct.EnotElement;

public class Asn1ElementValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element) {

        if(!Asn1TypeSpecification.TYPE_NAME.equalsIgnoreCase(element.getType())) {
            throw new EnotUnsupportedElementTypeException("Unsupported element type. Expecting ASN.1");
        }

        Object tagObject = element.getAttributes().get(Asn1Attribute.TAG.getName());
        if (tagObject instanceof String tagString) {
            Asn1Tag tag = Asn1Tag.fromString(tagString);
            validateAttributes(tag, element);
        } else {
            throw new EnotInvalidAttributeException("For ASN.1 elements tag attribute must be of string type");
        }
    }

    private void validateAttributes(Asn1Tag tag, EnotElement element) {

        element.getAttributes().forEach((key, value) -> {

        });
    }
}
