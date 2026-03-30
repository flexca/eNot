package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.util.PlaceholderUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EnotSerializer {

    private final EnotRegistry enotRegistry;
    private final EnotParser enotParser;

    public EnotSerializer(EnotRegistry enotRegistry, EnotParser enotParser) {
        this.enotRegistry = enotRegistry;
        this.enotParser = enotParser;
    }

    public List<byte[]> serialize(String json, Map<String, Object> parameters) throws EnotParsingException, EnotSerializationException {
        List<EnotElement> elements = enotParser.parse(json);
        return serialize(elements, parameters);
    }

    public List<byte[]> serialize(List<EnotElement> elements, Map<String, Object> parameters) throws EnotSerializationException {

        List<byte[]> output = new ArrayList<>();
        for (EnotElement element : elements) {
            byte[] serializationResult = serialize(element, parameters);
            output.add(serializationResult);
        }
        return output;
    }

    public byte[] serialize(EnotElement element, Map<String, Object> parameters) throws EnotSerializationException {


        return null;
    }

    private List<Object> serializeElement(EnotElement element, Map<String, Object> parameters) {

        Object objectBody = element.getBody();
        if (objectBody instanceof Collection<?> bodyCollection) {
            for(Object child : bodyCollection) {

            }
        } else if (objectBody instanceof EnotElement childElement) {
            serializeElement();
        } else {
            if (PlaceholderUtils.isPlaceholder(objectBody)) {

            }
        }
    }
}
