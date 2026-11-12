package io.github.flexca.enot.core.types.system.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.List;

public class SystemRandomGeneratorValidator implements EnotElementValidator {

    public static final long ABSOLUTE_MAX_LENGTH = 4096;

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        Object minObject = element.getAttribute(SystemAttribute.RANDOM_STRING_LENGTH);
        if (minObject == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath,
                    "missing required attribute: [" + SystemAttribute.RANDOM_STRING_LENGTH.getName() + "]"));
            return;
        }

        if (minObject instanceof Number randomStringLength) {
            long requestedLength = randomStringLength.longValue();
            if (requestedLength <= 0) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "attribute " + SystemAttribute.RANDOM_STRING_LENGTH.getName()
                        + " must be greater than zero"));
            } else if (requestedLength > SystemRandomGeneratorValidator.ABSOLUTE_MAX_LENGTH) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "attribute " + SystemAttribute.RANDOM_STRING_LENGTH.getName()
                        + " must be less or equals to " + SystemRandomGeneratorValidator.ABSOLUTE_MAX_LENGTH));
            }
        } else {
            jsonErrors.add(EnotJsonError.of(attributesPath, "attribute " + SystemAttribute.RANDOM_STRING_LENGTH.getName()
                    + " must be of type integer"));
        }
    }
}
