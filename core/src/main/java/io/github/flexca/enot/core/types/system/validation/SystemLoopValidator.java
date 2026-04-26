package io.github.flexca.enot.core.types.system.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.types.system.attribute.Uniqueness;
import io.github.flexca.enot.core.util.PlaceholderUtils;

import java.util.List;

public class SystemLoopValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        Object itemsNameObject = element.getAttribute(SystemAttribute.ITEMS_NAME);
        if (itemsNameObject == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath,
                    "missing required attribute: [" + SystemAttribute.ITEMS_NAME.getName() + "]"));
            return;
        }

        if (itemsNameObject instanceof String itemsName) {
            if (!PlaceholderUtils.isValidVariableName(itemsName, false)) {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.ITEMS_NAME.getName(),
                        "attribute " + SystemAttribute.ITEMS_NAME.getName() + " is not valid, use letters, digits or underscore"));
                return;
            }
        } else {
            jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.ITEMS_NAME.getName(),
                    "attribute " + SystemAttribute.ITEMS_NAME.getName() + " must be of type string"));
            return;
        }

        Object minItemsObject = element.getAttribute(SystemAttribute.MIN_ITEMS);
        Long minItems = null;
        if (minItemsObject != null) {
            if (minItemsObject instanceof Number minItemsNumber) {
                minItems = minItemsNumber.longValue();
                if (minItems < 0) {
                    jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.MIN_ITEMS.getName(),
                            "attribute " + SystemAttribute.MIN_ITEMS.getName() + " must be not negative integer"));
                }
            } else {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.MIN_ITEMS.getName(),
                        "attribute " + SystemAttribute.MIN_ITEMS.getName() + " must be of type integer"));
            }
        }

        Object maxItemsObject = element.getAttribute(SystemAttribute.MAX_ITEMS);
        Long maxItems = null;
        if (maxItemsObject != null) {
            if (maxItemsObject instanceof Number maxItemsNumber) {
                maxItems = maxItemsNumber.longValue();
                if (maxItems < 0) {
                    jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.MAX_ITEMS.getName(),
                            "attribute " + SystemAttribute.MAX_ITEMS.getName() + " must be not negative integer"));
                }
            } else {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.MAX_ITEMS.getName(),
                        "attribute " + SystemAttribute.MAX_ITEMS.getName() + " must be of type integer"));
            }
        }

        if (minItems != null && maxItems != null) {
            if (minItems > maxItems) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "attribute " + SystemAttribute.MIN_ITEMS.getName()
                        + " must be less than or equals to " + SystemAttribute.MAX_ITEMS.getName()));
            }
        }

        Object uniquenessObject = element.getAttribute(SystemAttribute.UNIQUENESS);
        if (uniquenessObject != null) {
            if (uniquenessObject instanceof String uniquenessString) {
                Uniqueness uniqueness = Uniqueness.fromName(uniquenessString);
                if (uniqueness == null) {
                    jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.UNIQUENESS.getName(),
                            "attribute " + SystemAttribute.UNIQUENESS.getName() + " must be one of " + Uniqueness.getNames()));
                }
            } else {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.UNIQUENESS.getName(),
                        "attribute " + SystemAttribute.UNIQUENESS.getName() + " must be of type string"));
            }
        }
    }
}
