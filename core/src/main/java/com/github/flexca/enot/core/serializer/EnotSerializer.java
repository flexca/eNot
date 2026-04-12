package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotBinaryConverter;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EnotSerializer {

    public static final String COMMON_ERROR_MESSAGE = "Failure during serialization: ";

    private final EnotRegistry enotRegistry;
    private final EnotParser enotParser;
    private final ConditionExpressionEvaluator conditionExpressionEvaluator;

    public EnotSerializer(EnotRegistry enotRegistry, EnotParser enotParser, ConditionExpressionEvaluator conditionExpressionEvaluator) {
        this.enotRegistry = enotRegistry;
        this.enotParser = enotParser;
        this.conditionExpressionEvaluator = conditionExpressionEvaluator;
    }

    public List<byte[]> serialize(String json, SerializationContext context) throws EnotParsingException, EnotSerializationException {
        List<EnotElement> elements = enotParser.parse(json);
        if(CollectionUtils.isEmpty(elements)) {
            return Collections.emptyList();
        }
        return elements.size() > 1 ? serialize(elements, context) : serialize(elements.get(0), context);
    }

    public List<byte[]> serialize(List<EnotElement> elements, SerializationContext context) throws EnotSerializationException {

        List<byte[]> serializationResult = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            EnotElement element = elements.get(i);
            serializationResult.addAll(serializeElement(element, context, "/" + i));
        }

        return serializationResult;
    }

    public List<byte[]> serialize(EnotElement element, SerializationContext context) throws EnotSerializationException {
        return serializeElement(element, context, "");
    }

    private List<byte[]> serializeElement(EnotElement element, SerializationContext context, String jsonPath) throws EnotSerializationException {

        EnotTypeSpecification typeSpecification = enotRegistry.getTypeSpecification(element.getType()).orElseThrow(() ->
            new EnotSerializationException(COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                    "cannot resolve EnotTypeSpecification for element with type " + element.getType())));

        ElementSerializer elementSerializer = typeSpecification.getSerializer(element);
        if(elementSerializer == null) {
            throw new EnotSerializationException(COMMON_ERROR_MESSAGE, EnotJsonError.of(jsonPath,
                    "serializer not found for element"));
        }
        List<ElementSerializationResult> serializationResults = elementSerializer.serialize(element, context, jsonPath,
                enotRegistry, conditionExpressionEvaluator);

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
