package com.github.flexca.enot.core.types.system.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.BaseElementSerializer;
import com.github.flexca.enot.core.serializer.ElementSerializationResult;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;

import java.util.*;

public class SystemLoopSerializer extends BaseElementSerializer {

    public List<ElementSerializationResult> serialize(EnotElement loopElement, SerializationContext context, String jsonPath,
                                                      EnotRegistry enotRegistry) throws EnotSerializationException {

        String itemsName = (String) loopElement.getAttributes().get(SystemAttribute.ITEMS_NAME);

        context.pathStepForward(itemsName);

        List<ElementSerializationResult> result = new ArrayList<>();
        while(context.hasNext()) {
            result.addAll(serializeBody(loopElement.getBody(), context, jsonPath, enotRegistry));
            context.nextIndex();
        }
        context.pathStepBack();

        return result;
    }
}
