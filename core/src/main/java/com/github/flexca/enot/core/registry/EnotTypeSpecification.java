package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.EnotValueType;

import java.util.List;

/**
 * Enot element type specification is used to describe specific type of elements. Used during parsing and validation
 * to ensure correct attributes names and values passed and element body is expected type
 */
public interface EnotTypeSpecification {

    /**
     * Provide information about name of elements type described by this specification
     * @return name of elements type, implementation must not return null or blank value
     */
    String getTypeName();

    /**
     * Provide list of additional value types
     * @return list of ValueType specific for this eNot element. Can return null
     */
    List<EnotValueType> getValueTypes();

    /**
     * Resolve attribute by its name and provide information about attribute name and value type
     * @param name - name of attribute
     * @return EnotAttribute object, implementation must not return null
     */
    EnotAttribute resolveAttributeByName(String name);

    /**
     * Provide specification of element used in generic validation for attributes and body of the element, specification
     * contains information about consume and produce types and also required and allowed attributes.
     * @param element - element to get specification for
     * @return EnotElementSpecification object, when implementation return null then no generic validation is performed
     * for attributes and body value of the element
     */
    EnotElementSpecification getElementSpecification(EnotElement element);

    /**
     * Provide specific for the type of elements validator that will be called after generic validator pass
     * @return EnotElementValidator object, when return null then no element specific validation is performed
     */
    EnotElementValidator getElementValidator();
}
