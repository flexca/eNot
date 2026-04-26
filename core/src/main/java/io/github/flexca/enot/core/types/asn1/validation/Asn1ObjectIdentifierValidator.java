package io.github.flexca.enot.core.types.asn1.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.util.OidUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Asn1ObjectIdentifierValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
        String allowedValuesPath = attributesPath + "/" + Asn1Attribute.ALLOWED_VALUES.getName();

        Object allowedValuesObject = element.getAttribute(Asn1Attribute.ALLOWED_VALUES);
        if(allowedValuesObject != null) {
            if (allowedValuesObject instanceof Collection<?> alloweValuesCollection) {
                int cntr = 0;
                Set<String> oids = new HashSet<>();
                Set<String> notUniqueOids = new HashSet<>();
                for(Object item : alloweValuesCollection) {
                    String oid = validateAndExtractSingleValue(item, allowedValuesPath + "/" + cntr, jsonErrors);
                    if(oid != null) {
                        if (!oids.add(oid)) {
                            notUniqueOids.add(oid);
                        }
                    }
                    cntr++;
                }
                if (!notUniqueOids.isEmpty()) {
                    jsonErrors.add(EnotJsonError.of(allowedValuesPath, "attribute " + Asn1Attribute.ALLOWED_VALUES.getName()
                            + " contains next not unique values: " + notUniqueOids));
                }
            } else {
                validateAndExtractSingleValue(allowedValuesObject, allowedValuesPath, jsonErrors);
            }
        }
    }

    private String validateAndExtractSingleValue(Object value, String path, List<EnotJsonError> jsonErrors) {

        if(value instanceof String valueString) {
            if(!OidUtils.isValidOid(valueString)) {
                jsonErrors.add(EnotJsonError.of(path, "attribute " + Asn1Attribute.ALLOWED_VALUES.getName()
                        + " must be valid OID"));
                return null;
            }
            return valueString;
        } else {
            jsonErrors.add(EnotJsonError.of(path, "attribute " + Asn1Attribute.ALLOWED_VALUES.getName()
                    + " must be string representing valid OID"));
            return null;
        }
    }

}
