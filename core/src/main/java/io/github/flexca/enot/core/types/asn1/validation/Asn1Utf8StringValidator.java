package io.github.flexca.enot.core.types.asn1.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;

import java.util.List;

public class Asn1Utf8StringValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;

        Long minLength = Asn1ValidationUtils.validateAndExtractMinLength(element, attributesPath, jsonErrors);
        Long maxLength = Asn1ValidationUtils.validateAndExtractMaxLength(element, attributesPath, jsonErrors);

        Asn1ValidationUtils.validateMinAndMaxLength(minLength, maxLength, attributesPath, jsonErrors);

        Asn1ValidationUtils.validateAndExtractAllowedTextValues(element, attributesPath, jsonErrors, false);
    }
}
