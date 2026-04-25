package io.github.flexca.enot.core.types.system.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.system.attribute.BitOrder;
import io.github.flexca.enot.core.types.system.attribute.ByteOrder;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.List;

public class SystemBitMapValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        Object byteOrderObject = element.getAttribute(SystemAttribute.BYTE_ORDER);
        if (byteOrderObject == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath,
                    "missing required attribute: [" + SystemAttribute.BYTE_ORDER.getName() + "]"));
        } else {
            if (byteOrderObject instanceof String byteOrderString) {
                ByteOrder byteOrder = ByteOrder.fromName(byteOrderString);
                if (byteOrder == null) {
                    jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.BYTE_ORDER.getName(),
                            "attribute " + SystemAttribute.BYTE_ORDER.getName() + " must be one of " + ByteOrder.getNames()));
                }
            } else {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.BYTE_ORDER.getName(),
                        "attribute " + SystemAttribute.BYTE_ORDER.getName() + " must be of type string"));
            }
        }

        Object bitOrderObject = element.getAttribute(SystemAttribute.BIT_ORDER);
        if (bitOrderObject == null) {
            jsonErrors.add(EnotJsonError.of(attributesPath,
                    "missing required attribute: [" + SystemAttribute.BIT_ORDER.getName() + "]"));
        } else {
            if (bitOrderObject instanceof String bitOrderString) {
                BitOrder bitOrder = BitOrder.fromName(bitOrderString);
                if (bitOrder == null) {
                    jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.BIT_ORDER.getName(),
                            "attribute " + SystemAttribute.BIT_ORDER.getName() + " must be one of " + BitOrder.getNames()));
                }
            } else {
                jsonErrors.add(EnotJsonError.of(attributesPath + "/" + SystemAttribute.BIT_ORDER.getName(),
                        "attribute " + SystemAttribute.BIT_ORDER.getName() + " must be of type string"));
            }
        }
    }
}
