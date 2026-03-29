package com.github.flexca.enot.core.system.validation;

import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotElementValidator;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.system.SystemKind;
import com.github.flexca.enot.core.system.SystemTypeSpecification;
import com.github.flexca.enot.core.system.attribute.SystemAttribute;

import java.util.List;

public class SystemElementValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors) {

        if(!SystemTypeSpecification.TYPE_NAME.equalsIgnoreCase(element.getType())) {
            jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_TYPE_NAME, "unsupported element type, expecting system"));
            return;
        }

        Object kindObject = element.getAttributes().get(SystemAttribute.KIND);
        if (!(kindObject instanceof String)) {
            jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME + "/" + SystemAttribute.KIND.getName(),
                    "invalid type of system elements attribute " + SystemAttribute.KIND.getName() + ", expecting string"));
            return;
        }

        SystemKind kind = SystemKind.fromString((String) kindObject);
        if (kind == null) {
            jsonErrors.add(EnotJsonError.of(parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME + "/" + SystemAttribute.KIND.getName(),
                    "unsupported value of system elements attribute " + SystemAttribute.KIND.getName()));
            return;
        }
    }
}
