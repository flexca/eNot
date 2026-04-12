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
 * An eNot element is a self-contained node in a tree-like structure. It consists of four parts:
 * <ul>
 *     <li><b>type:</b> A string identifying the kind of data the element represents (e.g., {@code "asn.1"}, {@code "system"}).</li>
 *     <li><b>optional:</b> A flag controlling serialization behaviour when the element's body value is absent.</li>
 *     <li><b>attributes:</b> A map of key-value pairs providing type-specific metadata about the element.</li>
 *     <li><b>body:</b> The actual content of the element. Depending on the type, this may be a primitive value
 *     (e.g., {@code String}, {@code BigDecimal}), a single nested {@link EnotElement}, a {@code List} of
 *     nested {@link EnotElement} objects, or {@code null} if the element has no body.</li>
 * </ul>
 * <p>
 * This class is a plain data object. All parsing, validation, and serialization behaviour is handled by
 * {@link com.github.flexca.enot.core.parser.EnotParser}, {@link com.github.flexca.enot.core.registry.EnotTypeSpecification},
 * and {@link com.github.flexca.enot.core.serializer.EnotSerializer}.
 * <p>
 * <b>Note on {@code equals} and {@code hashCode}:</b> All four fields — {@code type}, {@code optional},
 * {@code attributes}, and {@code body} — are included in {@code equals} and {@code hashCode}.
 * Two elements are considered equal only if their structure and optionality are identical.
 */
public class EnotElement {

    /**
     * The type identifier for the element. This string is used by the {@link com.github.flexca.enot.core.registry.EnotRegistry}
     * to look up the corresponding {@link com.github.flexca.enot.core.registry.EnotTypeSpecification} that defines
     * the element's behavior, validation rules, and expected body type.
     */
    private String type;

    /**
     * Controls the serialization behaviour when this element's body value is absent — either because
     * its placeholder was not provided in the parameters map, or because the resolved value is {@code null}.
     * <p>
     * <ul>
     *     <li>When {@code false} (the default): if the body value is absent during serialization,
     *     an {@link com.github.flexca.enot.core.exception.EnotSerializationException} is thrown.
     *     Use this for elements that are mandatory in the output structure.</li>
     *     <li>When {@code true}: if the body value is absent during serialization, the element and
     *     all of its children are silently skipped and produce no output bytes.
     *     Use this for elements that are conditionally present in the output structure.</li>
     * </ul>
     * <p>
     * Example: an ASN.1 {@code UTF8String} element with {@code optional = true} and body {@code "${subject_email}"}
     * will be included in the encoded output only when the {@code subject_email} placeholder value is supplied.
     * If omitted from the parameters map, the element is skipped without error.
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
        clone.setOptional(optional);
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

    public Object getAttribute(EnotAttribute attribute) {
        return attributes == null ? null : attributes.get(attribute);
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
        return optional == that.optional && Objects.equals(type, that.type) && Objects.equals(attributes, that.attributes) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, optional, attributes, body);
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
