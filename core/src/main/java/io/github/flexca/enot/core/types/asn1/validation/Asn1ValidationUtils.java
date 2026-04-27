package io.github.flexca.enot.core.types.asn1.validation;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.util.OidUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Asn1ValidationUtils {

    private Asn1ValidationUtils() {
    }

    public static Long validateAndExtractMinLength(EnotElement element, String attributesPath, List<EnotJsonError> jsonErrors) {

        String minLengthPath = attributesPath + "/" + Asn1Attribute.MIN_LENGTH.getName();

        Object minLengthObject = element.getAttribute(Asn1Attribute.MIN_LENGTH);
        if (minLengthObject == null) {
            return null;
        }

        if (minLengthObject instanceof Number minLengthNumber) {
            Long minLength = minLengthNumber.longValue();
            if (minLength < 0) {
                jsonErrors.add(EnotJsonError.of(minLengthPath, "attribute " + Asn1Attribute.MIN_LENGTH.getName()
                        + " must be not negative number"));
                return null;
            }
            return minLength;
        } else {
            jsonErrors.add(EnotJsonError.of(minLengthPath, "attribute " + Asn1Attribute.MIN_LENGTH.getName()
                    + " must be not negative number"));
            return null;
        }
    }

    public static Long validateAndExtractMaxLength(EnotElement element, String attributesPath, List<EnotJsonError> jsonErrors) {

        String maxLengthPath = attributesPath + "/" + Asn1Attribute.MAX_LENGTH.getName();

        Object maxLengthObject = element.getAttribute(Asn1Attribute.MAX_LENGTH);
        if (maxLengthObject == null) {
            return null;
        }

        if (maxLengthObject instanceof Number maxLengthNumber) {
            Long maxLength = maxLengthNumber.longValue();
            if (maxLength < 0) {
                jsonErrors.add(EnotJsonError.of(maxLengthPath, "attribute " + Asn1Attribute.MAX_LENGTH.getName()
                        + " must be not negative number"));
                return null;
            }
            return maxLength;
        } else {
            jsonErrors.add(EnotJsonError.of(maxLengthPath, "attribute " + Asn1Attribute.MAX_LENGTH.getName()
                    + " must be not negative number"));
            return null;
        }
    }

    public static void validateMinAndMaxLength(Long minLength, Long maxLength, String attributesPath, List<EnotJsonError> jsonErrors) {

        if (minLength != null && maxLength != null) {
            if (minLength > maxLength) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "value of attribute " + Asn1Attribute.MIN_LENGTH.getName()
                        + " must be less than or equals to attribute value " + Asn1Attribute.MIN_LENGTH.getName()));
            }
        }
    }

    public static Set<String> validateAndExtractAllowedTextValues(EnotElement element, String attributesPath, List<EnotJsonError> jsonErrors,
                                                                  boolean validateOids) {

        String allowedValuesPath = attributesPath + "/" + Asn1Attribute.ALLOWED_VALUES.getName();
        Object allowedValuesObject = element.getAttribute(Asn1Attribute.ALLOWED_VALUES);
        Set<String> uniqueValues = new HashSet<>();
        if (allowedValuesObject != null) {
            if (allowedValuesObject instanceof Collection<?> alloweValuesCollection) {
                int cntr = 0;

                Set<String> notUniqueValues = new HashSet<>();
                for (Object item : alloweValuesCollection) {
                    String oid = validateAndExtractSingleTextValue(item, allowedValuesPath + "/" + cntr, jsonErrors,
                            validateOids);
                    if (oid != null) {
                        if (!uniqueValues.add(oid)) {
                            notUniqueValues.add(oid);
                        }
                    }
                    cntr++;
                }
                if (!notUniqueValues.isEmpty()) {
                    jsonErrors.add(EnotJsonError.of(allowedValuesPath, "attribute " + Asn1Attribute.ALLOWED_VALUES.getName()
                            + " contains next not unique values: " + notUniqueValues));
                }
            } else {
                String allowedValue = validateAndExtractSingleTextValue(allowedValuesObject, allowedValuesPath, jsonErrors, validateOids);
                if (allowedValue != null) {
                    uniqueValues.add(allowedValue);
                }
            }
        }
        return uniqueValues;
    }

    public static void validateMinAndMaxLengthForTextDuringSerialization(EnotElement element, String jsonPath, String textBody) throws EnotSerializationException {

        String attributesPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
        String bodyPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;
        List<EnotJsonError> jsonErrors = new ArrayList<>();
        Long minLength = Asn1ValidationUtils.validateAndExtractMinLength(element, attributesPath, jsonErrors);
        if (!jsonErrors.isEmpty()) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, jsonErrors.get(0));
        }
        if (minLength != null && textBody.length() < minLength) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyPath,
                    "body length less than minimum allowed value: " + minLength));
        }
        Long maxLength = Asn1ValidationUtils.validateAndExtractMaxLength(element, attributesPath, jsonErrors);
        if (!jsonErrors.isEmpty()) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, jsonErrors.get(0));
        }
        if (maxLength != null && textBody.length() > maxLength) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyPath,
                    "body length greater than maximum allowed value: " + maxLength));
        }
    }

    public static void validateAllowedValuesForTextDuringSerialization(EnotElement element, String jsonPath, String textBody) throws EnotSerializationException {

        String attributesPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
        String bodyPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;
        List<EnotJsonError> jsonErrors = new ArrayList<>();
        Set<String> allowedValues = Asn1ValidationUtils.validateAndExtractAllowedTextValues(element, attributesPath,
                jsonErrors, false);
        if (!allowedValues.isEmpty() && !allowedValues.contains(textBody)) {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(bodyPath,
                    "body value is not allowed"));
        }
    }

    private static String validateAndExtractSingleTextValue(Object value, String path, List<EnotJsonError> jsonErrors,
                                                            boolean validateOids) {

        if (value instanceof String valueString) {
            if (validateOids && !OidUtils.isValidOid(valueString)) {
                jsonErrors.add(EnotJsonError.of(path, "attribute " + Asn1Attribute.ALLOWED_VALUES.getName()
                        + " must be valid OID"));
                return null;
            }
            return valueString;
        } else {
            if (validateOids) {
                jsonErrors.add(EnotJsonError.of(path, "attribute " + Asn1Attribute.ALLOWED_VALUES.getName()
                        + " must be string representing valid OID"));
            } else {
                jsonErrors.add(EnotJsonError.of(path, "attribute " + Asn1Attribute.ALLOWED_VALUES.getName()
                        + " must be string"));
            }
            return null;
        }
    }
}
