package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.util.PlaceholderUtils;

import java.math.BigInteger;
import java.util.*;

public abstract class BaseElementSerializer implements ElementSerializer {

    protected List<ElementSerializationResult> serializeBody(Object body, Map<String, Object> parameters, String jsonPath,
                                                             EnotRegistry enotRegistry) throws EnotSerializationException {

        List<ElementSerializationResult> result = new ArrayList<>();
        if (body instanceof Collection<?> children) {
            for (Object child : children) {
                if (child instanceof EnotElement childElement) {
                    result.addAll(serializeBodyElement(childElement, parameters, jsonPath, enotRegistry));
                } else {
                    result.addAll(serializeBodyPrimitive(child, parameters, jsonPath, enotRegistry));
                }
            }
        } else if (body instanceof EnotElement child) {
            result.addAll(serializeBodyElement(child, parameters, jsonPath, enotRegistry));
        } else {
            result.addAll(serializeBodyPrimitive(body, parameters, jsonPath, enotRegistry));
        }
        return result;
    }

    private List<ElementSerializationResult> serializeBodyElement(EnotElement element, Map<String, Object> parameters, String jsonPath,
                                                                  EnotRegistry enotRegistry) throws EnotSerializationException {

        EnotTypeSpecification typeSpecification = enotRegistry.getTypeSpecification(element.getType()).orElseThrow(() ->
                new EnotSerializationException(EnotSerializer.COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                        "cannot find EnotTypeSpecification for element of type " + element.getType())));

        ElementSerializer elementSerializer = typeSpecification.getSerializer(element);
        return elementSerializer.serialize(element, parameters, jsonPath, enotRegistry);
    }

    private List<ElementSerializationResult> serializeBodyPrimitive(Object body, Map<String, Object> parameters, String jsonPath,
                                                                    EnotRegistry enotRegistry) throws EnotSerializationException {

        Object value;
        Optional<String> placeholder = PlaceholderUtils.extractPlaceholder(body);
        if (placeholder.isPresent()) {
            value = parameters.get(placeholder.get());
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
