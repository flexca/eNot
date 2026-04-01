package com.github.flexca.enot.core.types.asn1.validation;

import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.types.asn1.Asn1Tag;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.element.EnotElement;

import java.util.List;

public class Asn1ElementValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors) {

        if(!Asn1TypeSpecification.TYPE_NAME.equalsIgnoreCase(element.getType())) {
            jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_TYPE_NAME, "unsupported element type, expecting asn.1"));
            return;
        }

        Object tagObject = element.getAttributes().get(Asn1Attribute.TAG);
        if (!(tagObject instanceof String)) {
            jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME + "/" + Asn1Attribute.TAG.getName(),
                    "invalid type of asn.1 elements attribute " + Asn1Attribute.TAG.getName() + ", expecting string"));
            return;
        }

        Asn1Tag tag = Asn1Tag.fromString((String) tagObject);
        if (tag == null) {
            jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME + "/" + Asn1Attribute.TAG.getName(),
                    "unsupported value of asn.1 elements attribute " + Asn1Attribute.TAG.getName()));
            return;
        }

        if (element.getAttributes().containsKey(Asn1Attribute.EXPLICIT) && element.getAttributes().containsKey(Asn1Attribute.IMPLICIT)) {
            jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME,
                    "both implicit and explicit attributes are not allowed for single asn.1 element"));
        }
    }
}
