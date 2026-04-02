package com.github.flexca.enot.core.types.system.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.BaseElementSerializer;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.*;

public class SystemLoopSerializer extends BaseElementSerializer {

    public List<ElementSerializationResult> serialize(EnotElement loopElement, Map<String, Object> parameters, String jsonPath,
                                                      EnotRegistry enotRegistry) throws EnotSerializationException {

        String itemsName = (String) loopElement.getAttributes().get(SystemAttribute.ITEMS_NAME);

        Object loopData = parameters.get(itemsName);

        List<ElementSerializationResult> result = new ArrayList<>();
        if (loopData instanceof Collection<?> collectionLoopData) {
            for (Object childLoopData : collectionLoopData) {
                if (childLoopData instanceof Map<?, ?>) {
                    result.addAll(serializeBody(loopElement.getBody(), (Map<String, Object>) childLoopData, jsonPath,
                            enotRegistry));
                } else {
                    result.addAll(serializeBody(loopElement.getBody(), Collections.emptyMap(), jsonPath,
                            enotRegistry));
                }
            }
        } else {
            if (loopData instanceof Map<?, ?>) {
                result.addAll(serializeBody(loopElement.getBody(), (Map<String, Object>) loopData, jsonPath,
                        enotRegistry));
            } else {
                result.addAll(serializeBody(loopElement.getBody(), Collections.emptyMap(), jsonPath,
                        enotRegistry));
            }
        }
        return result;
    }
}
