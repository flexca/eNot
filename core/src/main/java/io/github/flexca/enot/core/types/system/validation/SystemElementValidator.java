package io.github.flexca.enot.core.types.system.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.types.system.SystemKind;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.List;

public class SystemElementValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

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

        EnotElementValidator specificElementValidator = kind.getSpecificElementValidator();
        if (specificElementValidator != null) {
            specificElementValidator.validateElement(element, parentPath, jsonErrors, enotContext);
        }
    }
}
