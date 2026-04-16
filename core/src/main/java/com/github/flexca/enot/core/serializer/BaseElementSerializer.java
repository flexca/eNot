package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.util.PlaceholderUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base for all element serializers, providing recursive body traversal
 * and placeholder resolution.
 *
 * <p>An element body can take three structural forms, all handled transparently:</p>
 * <ul>
 *   <li><b>Collection</b> — a list of child elements or primitive values (e.g. the
 *       items of a SEQUENCE or the boolean flags of a bit_map).</li>
 *   <li><b>Single {@link EnotElement}</b> — one nested element whose serializer is
 *       looked up from the registry and invoked recursively.</li>
 *   <li><b>Primitive</b> — a literal value or a {@code ${placeholder}} string that
 *       is resolved against the current {@link SerializationContext} and wrapped in
 *       an {@link ElementSerializationResult} with the appropriate
 *       {@link CommonEnotValueType}.</li>
 * </ul>
 *
 * <p>Subclasses call {@link #serializeBody} to obtain the already-serialized children
 * and then decide how to combine them into the final output (e.g. encode as a DER
 * SEQUENCE, pack into a byte array, etc.).</p>
 */
public abstract class BaseElementSerializer implements ElementSerializer {

    /**
     * Recursively serializes the body of an element.
     *
     * <p>Dispatches to one of three paths depending on the runtime type of {@code body}:
     * <ol>
     *   <li>{@link Collection} — each item is serialized as a child element or primitive.</li>
     *   <li>{@link EnotElement} — the element's serializer is looked up and called.</li>
     *   <li>Everything else — treated as a primitive value or placeholder string.</li>
     * </ol>
     * </p>
     *
     * @param body        the raw body object from the parsed element
     * @param context     the active serialization context for placeholder resolution
     * @param jsonPath    JSON Pointer prefix used in error messages
     * @param enotContext the registry and shared configuration
     * @return the serialized fragments from all body children; never {@code null}
     * @throws EnotSerializationException if any child cannot be serialized
     */
    protected List<ElementSerializationResult> serializeBody(Object body, SerializationContext context, String jsonPath,
                                                             EnotContext enotContext) throws EnotSerializationException {

        List<ElementSerializationResult> result = new ArrayList<>();
        if (body instanceof Collection<?> children) {
            for (Object child : children) {
                if (child instanceof EnotElement childElement) {
                    result.addAll(serializeBodyElement(childElement, context, jsonPath, enotContext));
                } else {
                    result.addAll(serializeBodyPrimitive(child, context, jsonPath, enotContext));
                }
            }
        } else if (body instanceof EnotElement child) {
            result.addAll(serializeBodyElement(child, context, jsonPath, enotContext));
        } else {
            result.addAll(serializeBodyPrimitive(body, context, jsonPath, enotContext));
        }
        return result;
    }

    private List<ElementSerializationResult> serializeBodyElement(EnotElement element, SerializationContext context, String jsonPath,
                                                                  EnotContext enotContext) throws EnotSerializationException {

        EnotTypeSpecification typeSpecification = enotContext.getEnotRegistry().getTypeSpecification(element.getType()).orElseThrow(() ->
                new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "cannot find EnotTypeSpecification for element of type " + element.getType())));

        ElementSerializer elementSerializer = typeSpecification.getSerializer(element);
        return elementSerializer.serialize(element, context, jsonPath, enotContext);
    }

    private List<ElementSerializationResult> serializeBodyPrimitive(Object body, SerializationContext context, String jsonPath,
                                                                    EnotContext enotContext) throws EnotSerializationException {

        Object value;
        Optional<String> placeholder = PlaceholderUtils.extractPlaceholder(body);
        if (placeholder.isPresent()) {
            value = context.resolvePlaceholderValue(placeholder.get());
        } else {
            value = body;
        }

        if (value == null) {
            return Collections.emptyList();
        }

        List<ElementSerializationResult> serializationResults = new ArrayList<>();
        if (value instanceof Collection<?> collectionValue) {
            int i = 0;
            for(Object child : collectionValue) {
                serializationResults.add(serializeSinglePrimitiveValue(child, jsonPath + "/" + i));
                i++;
            }
        } else {
            serializationResults.add(serializeSinglePrimitiveValue(value, jsonPath));
        }
        return serializationResults;
    }

    private ElementSerializationResult serializeSinglePrimitiveValue(Object value, String jsonPath) throws EnotSerializationException{

        if (value instanceof Boolean) {
            return ElementSerializationResult.of(CommonEnotValueType.BOOLEAN, value);
        } else if (value instanceof byte[]) {
            return ElementSerializationResult.of(CommonEnotValueType.BINARY, value);
        } else if ((value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger)) {
            return ElementSerializationResult.of(CommonEnotValueType.INTEGER, value);
        } else if (value instanceof String) {
            return ElementSerializationResult.of(CommonEnotValueType.TEXT, value);
        } else {
            throw new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                    "unsupported value type: " + value.getClass().getName()));
        }
    }
}
