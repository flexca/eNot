package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.context.SerializationContext;

import java.util.List;
import java.util.Map;

public abstract class SimpleElementSerializer extends BaseElementSerializer {


    protected abstract List<ElementSerializationResult> serialize(EnotElement element, List<ElementSerializationResult> serializedBody,
                                                                  String jsonPath) throws EnotSerializationException;

    @Override
    public List<ElementSerializationResult> serialize(EnotElement element, SerializationContext context, String jsonPath,
                                                      EnotRegistry enotRegistry) throws EnotSerializationException {

        String currentJsonPath = jsonPath + "/" + EnotParser.ENOT_ELEMENT_BODY_NAME;
        List<ElementSerializationResult> serializedBody = serializeBody(element.getBody(), context, currentJsonPath, enotRegistry);
        return serialize(element, serializedBody, currentJsonPath);
    }
}
