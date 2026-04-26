package io.github.flexca.enot.core.types.asn1.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.asn1.Asn1Tag;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;

import java.util.List;

public class Asn1TaggedObjectValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        Object explicitObject = element.getAttribute(Asn1Attribute.EXPLICIT);
        Long explicit = null;
        if (explicitObject != null) {
            if(explicitObject instanceof Number explicitNumber) {
                explicit = explicitNumber.longValue();
                if (explicit < 0) {
                    jsonErrors.add(EnotJsonError.of(attributesPath + "/" + Asn1Attribute.EXPLICIT.getName(),
                            "explicit attribute must be not negative integer"));
                    return;
                }
            } else {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + Asn1Attribute.EXPLICIT.getName(),
                        "explicit attribute must be not negative integer"));
                return;
            }
        }

        Object implicitObject = element.getAttribute(Asn1Attribute.IMPLICIT);
        Long implicit = null;
        if (implicitObject != null) {
            if(implicitObject instanceof Number implicitNumber) {
                implicit = implicitNumber.longValue();
                if (implicit < 0) {
                    jsonErrors.add(EnotJsonError.of(attributesPath + "/" + Asn1Attribute.IMPLICIT.getName(),
                            "implicit attribute must be not negative integer"));
                    return;
                }
            } else {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + Asn1Attribute.IMPLICIT.getName(),
                        "implicit attribute must be not negative integer"));
                return;
            }
        }

        if (explicit == null && implicit == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath, "at least one implicit or explicit attributes must be provided"
                    + Asn1Tag.TAGGED_OBJECT.getName() + " element"));
        }

        if (explicit != null && implicit != null) {
            jsonErrors.add(EnotJsonError.of(attributesPath, "both implicit and explicit attributes are not allowed for "
                    + Asn1Tag.TAGGED_OBJECT.getName() + " element"));
        }
    }
}
