package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.lang3.SerializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EnotSerializer {

    public static final String COMMON_ERROR_MESSAGE = "Failure during serialization: ";

    private final EnotRegistry enotRegistry;
    private final EnotParser enotParser;

    public EnotSerializer(EnotRegistry enotRegistry, EnotParser enotParser) {
        this.enotRegistry = enotRegistry;
        this.enotParser = enotParser;
    }

    public ElementSerializationResult serialize(String json, Map<String, Object> parameters) throws EnotParsingException, EnotSerializationException {
        List<EnotElement> elements = enotParser.parse(json);
        return serialize(elements, parameters);
    }

    public List<byte[]> serialize(List<EnotElement> elements, Map<String, Object> parameters) throws EnotSerializationException {

        List<Object> serializationResult = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            EnotElement element = elements.get(i);
            serializationResult.addAll(serializeElement(element, parameters, "/" + i));
        }

        List<byte[]> output = new ArrayList<>();
        return output;
    }

    public byte[] serialize(EnotElement element, Map<String, Object> parameters) throws EnotSerializationException {

        List<Object> serializationResult = serializeElement(element, parameters, "");
        return null;
    }

    private List<Object> serializeElement(EnotElement element, Map<String, Object> parameters, String jsonPath) {

        EnotTypeSpecification typeSpecification = enotRegistry.getTypeSpecification(element.getType()).orElseThrow(() -> {
            new EnotSerializationException();
        });

        ElementSerializer elementSerializer = typeSpecification.getSerializer(element);
        List<Object> serializationResult = elementSerializer.serialize(element, parameters, jsonPath, );
        return null;
    }
}
