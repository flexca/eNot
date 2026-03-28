package com.github.flexca.enot.core.struct.attribute;

import com.github.flexca.enot.core.struct.value.EnotValueSpecification;
import com.github.flexca.enot.core.struct.value.EnotValueType;

/**
 * Defines a contract for an attribute key used in an {@link com.github.flexca.enot.core.struct.EnotElement}.
 * <p>
 * An attribute provides metadata about an element. Each attribute is defined by a unique name and a value
 * specification that describes the expected value type and whether multiple values are allowed.
 * This interface is typically implemented by an enum, where each enum constant represents a specific attribute.
 *
 * @see com.github.flexca.enot.core.struct.EnotElement#getAttributes()
 */
public interface EnotAttribute {

    /**
     * Gets the unique, case-insensitive name of the attribute.
     * <p>
     * This name is used as the key in the JSON representation of the element's attributes
     * and is matched case-insensitively during parsing.
     *
     * @return The non-null, non-blank name of the attribute.
     */
    String getName();

    /**
     * Gets the value specification for this attribute, describing the expected {@link EnotValueType}
     * and whether the attribute accepts multiple values (a JSON array).
     * <p>
     * The {@link com.github.flexca.enot.core.parser.EnotParser} uses this to validate that the value
     * provided in a JSON payload conforms to the expected type (e.g., {@code BOOLEAN}, {@code TEXT},
     * {@code INTEGER}) and array/single-value contract.
     *
     * @return The non-null {@link EnotValueSpecification} for this attribute.
     */
    EnotValueSpecification getValueSpecification();

    /**
     * A factory-like method to retrieve an attribute instance by its string name.
     * <p>
     * This is typically implemented in enums to allow dynamic lookup of an attribute
     * by its name as parsed from a JSON payload. Implementations should perform a
     * case-insensitive match.
     *
     * @param name The name of the attribute to find.
     * @return The corresponding {@link EnotAttribute} instance, or {@code null} if no attribute
     *         with the given name exists.
     */
    EnotAttribute fromName(String name);
}
