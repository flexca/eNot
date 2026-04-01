package com.github.flexca.enot.core.types.system.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.ElementSerializer;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.*;

public class SystemLoopSerializer implements ElementSerializer {

    public List<ElementSerializationResult> serialize(EnotElement loopElement, Map<String, Object> parameters, String jsonPath,
                                                      List<String> paramPathStack, EnotRegistry enotRegistry) {

        String itemsName = (String) loopElement.getAttributes().get(SystemAttribute.ITEMS_NAME);
        List<String> currentParamPathStack = new ArrayList<>(paramPathStack);
        currentParamPathStack.add(itemsName);

        Object loopData = parameters.get(itemsName);

        List<Object> result = new ArrayList<>();
        if (loopData instanceof Collection<?> collectionLoopData) {
            for (Object childLoopData : collectionLoopData) {
                if (childLoopData instanceof Map<?, ?>) {
                    result.addAll(serializeBody(loopElement.getBody(), (Map<String, Object>) childLoopData, jsonPath,
                            currentParamPathStack, enotRegistry));
                } else {
                    result.addAll(serializeBody(loopElement.getBody(), Collections.emptyMap(), jsonPath,
                            currentParamPathStack, enotRegistry));
                }
            }
        } else {
            if (loopData instanceof Map<?, ?>) {
                result.addAll(serializeBody(loopElement.getBody(), (Map<String, Object>) loopData, jsonPath,
                        currentParamPathStack, enotRegistry));
            } else {
                result.addAll(serializeBody(loopElement.getBody(), Collections.emptyMap(), jsonPath,
                        currentParamPathStack, enotRegistry));
            }
        }
        return result;
    }

    private List<Object> serializeBody(Object body, Map<String, Object> parameters, String jsonPath,
                                       List<String> parametersPathStack, EnotRegistry enotRegistry) {

        List<Object> result = new ArrayList<>();
        if (body instanceof Collection<?> children) {
            for (Object child : children) {
                if (child instanceof EnotElement childElement) {
                    result.addAll(serializeBodyElement(childElement, parameters, parametersPathStack, enotRegistry));
                } else {
                    throw new EnotSerializationException("");
                }
            }
        } else if (body instanceof EnotElement child) {
            result.addAll(serializeBodyElement(child, parameters, parametersPathStack, enotRegistry));
        } else {
            throw new EnotSerializationException();
        }
        return result;
    }

    private List<Object> serializeBodyElement(EnotElement element, Map<String, Object> parameters, List<String> parametersPathStack,
                                              EnotRegistry enotRegistry) {

        EnotTypeSpecification typeSpecification = enotRegistry.getTypeSpecification(element.getType()).orElseThrow(() -> {
            new EnotSerializationException();
        });

        ElementSerializer elementSerializer = typeSpecification.getSerializer(element);
        return elementSerializer.serialize(element, parameters, parametersPathStack, enotRegistry);
    }
}
