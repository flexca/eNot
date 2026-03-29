package com.github.flexca.enot.core.element;

import com.github.flexca.enot.core.element.attribute.EnotAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a fundamental building block within the eNot data structure.
 * <p>
 * An eNot element is a self-contained node in a tree-like structure. It consists of three main parts:
 * <ul>
 *     <li><b>type:</b> A string that identifies the kind of data the element represents (e.g., "asn1.SEQUENCE", "string", "binary").</li>
 *     <li><b>attributes:</b> A map of key-value pairs that provide metadata about the element.</li>
 *     <li><b>body:</b> The actual content or payload of the element. The nature of the body depends on the element's type.
 *     It can be a simple value (like a String or byte array), a list of nested {@link EnotElement} objects, or null if it has no body.</li>
 * </ul>
 * This class is a simple POJO, with behavior and validation being handled by the {@link com.github.flexca.enot.core.parser.EnotParser}
 * and {@link com.github.flexca.enot.core.registry.EnotTypeSpecification} implementations.
 */
public class EnotElement {

    /**
     * The type identifier for the element. This string is used by the {@link com.github.flexca.enot.core.registry.EnotRegistry}
     * to look up the corresponding {@link com.github.flexca.enot.core.registry.EnotTypeSpecification} that defines
     * the element's behavior, validation rules, and expected body type.
     */
    private String type;

    /**
     * Optional defines should be element serialized when no data provided in placeholders
     */
    private boolean optional = false;

    /**
     * A map containing metadata about the element. Attributes provide additional, type-specific information
     * that is not part of the core body. The keys are defined as {@link EnotAttribute} instances, and the values
     * can be of any type, as validated by the element's {@link com.github.flexca.enot.core.registry.EnotTypeSpecification}.
     */
    private Map<EnotAttribute, Object> attributes;

    /**
     * The main payload or content of the element. The type of this object is determined by the element's 'type'
     * and its corresponding {@link com.github.flexca.enot.core.registry.EnotTypeSpecification}.
     */
    private Object body;

    public EnotElement copy() {

        EnotElement clone = new EnotElement();
        clone.setType(type);
        Map<EnotAttribute, Object> cloneAttributes = new HashMap<>(attributes);
        clone.setAttributes(cloneAttributes);
        if (body instanceof EnotElement elementBody) {
            EnotElement cloneBody = elementBody.copy();
            clone.setBody(cloneBody);
        } else if (body instanceof Collection<?> collectionBody) {
            List<Object> cloneBody = new ArrayList<>();
            for(Object child : collectionBody) {
                if(child instanceof EnotElement enotChild) {
                    cloneBody.add(enotChild.copy());
                } else {
                    cloneBody.add(child);
                }
            }
            clone.setBody(cloneBody);
        } else {
            clone.setBody(body);
        }
        return clone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public Map<EnotAttribute, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<EnotAttribute, Object> attributes) {
        this.attributes = attributes;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnotElement that = (EnotElement) o;
        return Objects.equals(type, that.type) && Objects.equals(attributes, that.attributes) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, attributes, body);
    }

    @Override
    public String toString() {
        return "EnotElement{" +
                "type='" + type + '\'' +
                ", attributes=" + attributes +
                ", body=" + body +
                '}';
    }

}
