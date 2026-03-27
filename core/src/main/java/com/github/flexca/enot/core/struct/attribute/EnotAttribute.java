package com.github.flexca.enot.core.struct.attribute;

import com.github.flexca.enot.core.struct.value.ValueType;
import lombok.Getter;

/**
 * Defines a contract for an attribute key used in an {@link com.github.flexca.enot.core.struct.EnotElement}.
 * <p>
 * An attribute provides metadata about an element. Each attribute is defined by a unique name and an expected
 * value type. This interface is typically implemented by an enum, where each enum constant represents a
 * specific attribute.
 *
 * @see com.github.flexca.enot.core.struct.EnotElement#getAttributes()
 * @see CommonEnotAttribute
 */
public interface EnotAttribute {

    /**
     * Gets the unique, case-sensitive name of the attribute.
     * <p>
     * This name is used as the key in the JSON representation of the element's attributes.
     *
     * @return The non-null name of the attribute.
     */
    String getName();

    /**
     * Gets the expected type of the attribute's value.
     * <p>
     * The {@link com.github.flexca.enot.core.parser.EnotParser} uses this information to validate that the
     * value provided for this attribute in a JSON payload has the correct type (e.g., BOOLEAN, STRING, INTEGER).
     *
     * @return The {@link ValueType} that this attribute's value must conform to.
     */
    ValueType getValueType();

    /**
     * A factory-like method to retrieve an attribute instance from its string name.
     * <p>
     * This is typically implemented in enums that implement this interface to allow for dynamic lookup
     * of an attribute based on its name parsed from a JSON payload.
     *
     * @param name The case-sensitive name of the attribute to find.
     * @return The corresponding {@link EnotAttribute} instance.
     * @throws java.util.NoSuchElementException if no attribute with the given name is found.
     */
    EnotAttribute fromName(String name);
}
