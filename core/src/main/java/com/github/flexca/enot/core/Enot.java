package com.github.flexca.enot.core;

import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class Enot {

    private final EnotContext enotContext;
    private final EnotParser enotParser;
    private final EnotSerializer enotSerializer;

    public Enot(EnotRegistry enotRegistry, ObjectMapper objectMapper) {
        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        ConditionExpressionEvaluator conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser);
        enotContext = new EnotContext(enotRegistry, conditionExpressionParser, conditionExpressionEvaluator);
        enotParser = new EnotParser(enotContext, objectMapper);
        enotSerializer = new EnotSerializer(enotContext, enotParser);
    }

    public List<EnotElement> parse(String json) throws EnotParsingException {
        return enotParser.parse(json);
    }

    public List<byte[]> serialize(String json, SerializationContext context) throws EnotParsingException, EnotSerializationException {
        return enotSerializer.serialize(json, context);
    }

    public List<byte[]> serialize(EnotElement element, SerializationContext context) throws EnotSerializationException {
        return enotSerializer.serialize(element, context);
    }

    public List<byte[]> serialize(List<EnotElement> elements, SerializationContext context) throws EnotSerializationException {
        return enotSerializer.serialize(elements, context);
    }

    public Map<String, Object> getParamsExample(String json) {
        return null;
    }

    public Map<String, Object> getParamsExample(EnotElement element) {
        return null;
    }

    public Map<String, Object> getParamsExample(List<EnotElement> elements) {
        return null;
    }
}
