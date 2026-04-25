package io.github.flexca.enot.core.types.system.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SystemReferenceValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        Object referenceTypeObject = element.getAttribute(SystemAttribute.REFERENCE_TYPE);
        if (referenceTypeObject == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath,
                    "missing required attribute: [" + SystemAttribute.REFERENCE_TYPE.getName() + "]"));
            return;
        }

        if (referenceTypeObject instanceof String referenceType) {
            if (!PlaceholderUtils.isValidVariableName(referenceType, false)) {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.REFERENCE_TYPE.getName(),
                        "attribute " + SystemAttribute.REFERENCE_TYPE.getName() + " is not valid, use letters, digits or underscore"));
                return;
            }
        } else {
            jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.REFERENCE_TYPE.getName(),
                    "attribute " + SystemAttribute.REFERENCE_TYPE.getName() + " must be of type string"));
            return;
        }

        Object referenceIdentifierObject = element.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER);
        if (referenceIdentifierObject == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath,
                    "missing required attribute: [" + SystemAttribute.REFERENCE_IDENTIFIER.getName() + "]"));
            return;
        }

        if (referenceIdentifierObject instanceof String referenceIdentifier) {
            if (StringUtils.isBlank(referenceIdentifier)) {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.REFERENCE_IDENTIFIER.getName(),
                        "attribute " + SystemAttribute.REFERENCE_IDENTIFIER.getName() + " must not be blank"));
            }
        } else {
            jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.REFERENCE_IDENTIFIER.getName(),
                    "attribute " + SystemAttribute.REFERENCE_IDENTIFIER.getName() + " must be of type string"));
        }

    }
}
