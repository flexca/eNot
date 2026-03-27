package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueSpecification;

import java.util.Set;

/**
 * Single element specification purpose is to describe element:
 * - what element consume - expected type of eNot element body
 * - what element produce - type of data produced when element is serialized
 * - what is required attributes
 * - what is allowed attributes
 */
public interface EnotElementSpecification {

    /**
     * Provide information about type of eNot element body it can consume. Required for validation to check that parent eNot
     * element can consume provided in JSON body during parsing and serialization.
     * @return ValueSpecification with EnotValueType and boolean flag allowMultipleValues, must not return null,
     * use CommonValueType.EMPY when element don't consume at all
     */
    ValueSpecification getConsumeType();

    /**
     * Provide information about type of data eNot element produce after serialization. Required for validation to check that parent eNot
     * element can consume provided in JSON body during parsing and serialization.
     * @return ValueSpecification with EnotValueType and boolean flag allowMultipleValues, must not return null.
     */
    ValueSpecification getProduceType();

    /**
     * Provide set of required attributes, if during validation all this attributes is not present in eNot element then
     * EnotParsingException will be thrown
     * @return Set of required attribute, can return null or empty Set, in both cases no generic validation will be performed
     */
    Set<EnotAttribute> getRequiredAttributes();

    /**
     * Provide set of allowed attributes, if during validation at least one of eNot element attributes is not in this Set -
     * then EnotParsingException will be thrown
     * @return Set of allowed attribute, can return null or empty Set, in both cases no generic validation will be performed
     */
    Set<EnotAttribute> getAllowedAttributes();
}
