package com.github.flexca.enot.bertlv.validator;

import com.github.flexca.enot.bertlv.BerTlvEnotTypeSpecification;
import com.github.flexca.enot.bertlv.attribute.BerTlvAttribute;
import com.github.flexca.enot.bertlv.util.BerTlvUtils;
import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotElementValidator;

import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

public class BerTlvElementValidator implements EnotElementValidator {

    private static final int MAX_TAG_LENGTH = 4;

    private final Set<String> tagsToIgnore;

    public BerTlvElementValidator(String... tagsToIgnore) {
        this.tagsToIgnore = tagsToIgnore == null ? Collections.emptySet() : Set.of(tagsToIgnore);
    }

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        String attributesPath = parentPath + "/" + EnotParser.ENOT_ELEMENT_ATTRIBUTES_NAME;
        String tagPath = attributesPath + "/" + BerTlvAttribute.TAG.getName();

        Object tagObject = element.getAttribute(BerTlvAttribute.TAG);
        if (tagObject == null) {
            jsonErrors.add(EnotJsonError.of(tagPath, "missing " + BerTlvAttribute.TAG.getName() + " attribute for " +
                    "eNot element of type " + BerTlvEnotTypeSpecification.TYPE));
            return;
        }

        byte[] tagBinary;
        if (tagObject instanceof String tag) {
            try {
                tagBinary = HexFormat.of().parseHex(tag);
            } catch (Exception e) {
                jsonErrors.add(EnotJsonError.of(tagPath, BerTlvAttribute.TAG.getName() + " attribute for eNot element of type "
                        + BerTlvEnotTypeSpecification.TYPE + " must be hex string"));
                return;
            }
        } else {
            jsonErrors.add(EnotJsonError.of(tagPath, BerTlvAttribute.TAG.getName() + " attribute for eNot element of type "
                    + BerTlvEnotTypeSpecification.TYPE + " must be hex string"));
            return;
        }

        if (tagBinary.length == 0) {
            jsonErrors.add(EnotJsonError.of(tagPath, BerTlvAttribute.TAG.getName() + " attribute for eNot element of type "
                    + BerTlvEnotTypeSpecification.TYPE + " must not be empty"));
        } else if (tagBinary.length > MAX_TAG_LENGTH) {
            jsonErrors.add(EnotJsonError.of(tagPath, "length of " + BerTlvAttribute.TAG.getName() + " attribute for eNot element of type "
                    + BerTlvEnotTypeSpecification.TYPE + " must not exceed 4 bytes or 8 octets"));
        }

        if (!tagsToIgnore.contains((String) tagObject)) {
            if (!BerTlvUtils.isValidTagLength(tagBinary)) {
                jsonErrors.add(EnotJsonError.of(tagPath, BerTlvAttribute.TAG.getName() + " attribute for eNot element of type "
                        + BerTlvEnotTypeSpecification.TYPE + " is formed incorrectly"));
                return;
            }
        }

        String minLengthPath = attributesPath + "/" + BerTlvAttribute.MIN_LENGTH.getName();
        Object minLengthObject = element.getAttribute(BerTlvAttribute.MIN_LENGTH);
        Integer minLength = null;
        if (minLengthObject != null) {
            if (minLengthObject instanceof Number minLengthInt) {
                if (minLengthInt.intValue() < 0) {
                    jsonErrors.add(EnotJsonError.of(minLengthPath, "attribute " + BerTlvAttribute.MIN_LENGTH.getName()
                            + " value must be positive integer"));
                } else {
                    minLength = minLengthInt.intValue();
                }
            } else {
                jsonErrors.add(EnotJsonError.of(minLengthPath, "attribute " + BerTlvAttribute.MIN_LENGTH.getName()
                        + " value must be positive integer"));
            }
        }

        String maxLengthPath = attributesPath + "/" + BerTlvAttribute.MAX_LENGTH.getName();
        Object maxLengthObject = element.getAttribute(BerTlvAttribute.MAX_LENGTH);
        Integer maxLength = null;
        if (maxLengthObject != null) {
            if (maxLengthObject instanceof Number maxLengthInt) {
                if (maxLengthInt.intValue() < 0) {
                    jsonErrors.add(EnotJsonError.of(maxLengthPath, "attribute " + BerTlvAttribute.MAX_LENGTH.getName()
                            + " value must be positive integer"));
                } else {
                    maxLength = maxLengthInt.intValue();
                }
            } else {
                jsonErrors.add(EnotJsonError.of(maxLengthPath, "attribute " + BerTlvAttribute.MAX_LENGTH.getName()
                        + " value must be positive integer"));
            }
        }

        if (minLength != null && maxLength != null) {
            if (minLength > maxLength) {
                jsonErrors.add(EnotJsonError.of(attributesPath, "attribute " + BerTlvAttribute.MIN_LENGTH.getName()
                        + " must be less or equals " + BerTlvAttribute.MAX_LENGTH.getName()));
            }
        }

        Object indefiniteFormObject = element.getAttribute(BerTlvAttribute.INDEFINITE_FORM);
        if (indefiniteFormObject != null && !(indefiniteFormObject instanceof Boolean)) {
            jsonErrors.add(EnotJsonError.of(attributesPath + "/" + BerTlvAttribute.INDEFINITE_FORM.getName(),
                    "attribute " + BerTlvAttribute.INDEFINITE_FORM.getName() + " must be boolean"));
        }

    }
}
