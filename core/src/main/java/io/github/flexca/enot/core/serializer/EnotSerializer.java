package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.exception.EnotParsingException;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotBinaryConverter;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.registry.EnotTypeSpecification;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnotSerializer {

    public static final String COMMON_ERROR_MESSAGE = "Failure during serialization: ";

    private final EnotParser enotParser;

    public EnotSerializer(EnotParser enotParser) {
        this.enotParser = enotParser;
    }

    public List<byte[]> serialize(String json, SerializationContext context, EnotContext enotContext) throws EnotParsingException, EnotSerializationException {
        List<EnotElement> elements = enotParser.parse(json, enotContext);
        if(CollectionUtils.isEmpty(elements)) {
            return Collections.emptyList();
        }
        return elements.size() > 1 ? serialize(elements, context, enotContext) : serialize(elements.get(0), context, enotContext);
    }

    public List<byte[]> serialize(List<EnotElement> elements, SerializationContext context, EnotContext enotContext) throws EnotSerializationException {

        List<byte[]> serializationResult = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            EnotElement element = elements.get(i);
            serializationResult.addAll(serializeElement(element, context, "/" + i, enotContext));
        }

        return serializationResult;
    }

    public List<byte[]> serialize(EnotElement element, SerializationContext context, EnotContext enotContext) throws EnotSerializationException {
        return serializeElement(element, context, "", enotContext);
    }

    private List<byte[]> serializeElement(EnotElement element, SerializationContext context, String jsonPath, EnotContext enotContext)
            throws EnotSerializationException {

        EnotTypeSpecification typeSpecification = enotContext.getEnotRegistry().getTypeSpecification(element.getType()).orElseThrow(() ->
            new EnotSerializationException(COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                    "cannot resolve EnotTypeSpecification for element with type " + element.getType())));

        ElementSerializer elementSerializer = typeSpecification.getSerializer(element);
        if(elementSerializer == null) {
            throw new EnotSerializationException(COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                    "serializer not found for element"));
        }
        List<ElementSerializationResult> serializationResults = elementSerializer.serialize(element, context, jsonPath, enotContext);

        List<byte[]> result = new ArrayList<>();
        for (ElementSerializationResult serializationResult : serializationResults) {
            EnotBinaryConverter binaryConverter = serializationResult.getValueType().getBinaryConverter();
            try {
                byte[] converted = binaryConverter.toBinary(serializationResult.getData());
                if (converted != null && converted.length > 0) {
                    result.add(converted);
                }
            } catch(Exception e) {
                throw new EnotSerializationException(COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath, e.getMessage()));
            }
        }
        return result;
    }
}
