package io.github.flexca.enot.core.types.system.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.util.PlaceholderUtils;

import java.util.List;

public class SystemGroupValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        Object groupNameObject = element.getAttribute(SystemAttribute.GROUP_NAME);
        if (groupNameObject == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath,
                    "missing required attribute: [" + SystemAttribute.GROUP_NAME.getName() + "]"));
            return;
        }

        if (groupNameObject instanceof String groupName) {
            if (!PlaceholderUtils.isValidVariableName(groupName, false)) {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.GROUP_NAME.getName(),
                        "attribute " + SystemAttribute.GROUP_NAME.getName() + " is not valid, use letters, digits or underscore"));
            }
        } else {
            jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.GROUP_NAME.getName(),
                    "attribute " + SystemAttribute.GROUP_NAME.getName() + " must be of type string"));
        }
    }
}
