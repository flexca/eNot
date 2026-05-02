package io.github.flexca.enot.core.registry;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.attribute.EnotAttribute;
import io.github.flexca.enot.core.element.value.EnotValueType;
import io.github.flexca.enot.core.serializer.ElementSerializer;

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
     * @return list of ValueType specific for this eNot elements type. Can return null
     */
    List<EnotValueType> getValueTypes();

    /**
     * Returns the list of all attributes recognised by this element type.
     *
     * <p>Used by the parser to enumerate the accepted attribute keys for this type.
     * The list must include both required and optional attributes.
     *
     * @return the complete list of supported attributes; may be empty but must not be {@code null}
     */
    List<EnotAttribute> getAttributes();

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
     * for attributes and body value of the element and validation fully depends on type specific EnotElementValidator
     * implementation
     */
    EnotElementSpecification getElementSpecification(EnotElement element);

    /**
     * Provide specific for the type of elements validator that will be called after generic validator pass
     * @return EnotElementValidator object, when return null then no element specific validation is performed
     */
    EnotElementValidator getElementValidator();

    /**
     * Return serializer for eNot element
     * @param element - element to get serializer for
     * @return serializer
     */
    ElementSerializer getSerializer(EnotElement element);

    /**
     * Returns path-altering instructions that the serializer applies when navigating
     * the parameter tree for the given element.
     *
     * <p>The default implementation returns {@link EnotElementPathAltering#none()},
     * meaning the parameter-tree cursor is not moved. Override this method only in
     * element types that drive their own path navigation (e.g. {@code loop}, {@code group}).
     *
     * @param element the element being serialized
     * @return path-altering instructions; must not return {@code null}
     */
    default EnotElementPathAltering getPathAltering(EnotElement element) {
        return EnotElementPathAltering.none();
    }
}
