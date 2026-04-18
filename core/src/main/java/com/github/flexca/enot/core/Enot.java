package com.github.flexca.enot.core;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.extractor.ExampleParamsExtractor;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.util.ParamUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Enot {

    private final EnotContext enotContext;
    private final EnotParser enotParser;
    private final EnotSerializer enotSerializer;
    private final ExampleParamsExtractor exampleParamsExtractor;
    private final ObjectMapper objectMapper;

    public Enot(EnotRegistry enotRegistry, ObjectMapper objectMapper) {

        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        ConditionExpressionEvaluator conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser);

        enotParser = new EnotParser(objectMapper);
        enotSerializer = new EnotSerializer(enotParser);
        enotContext = new EnotContext(enotRegistry, enotParser, enotSerializer, conditionExpressionParser, conditionExpressionEvaluator);
        exampleParamsExtractor = new ExampleParamsExtractor(enotContext);
        this.objectMapper = objectMapper;
    }

    public List<EnotElement> parse(String json) throws EnotParsingException {
        return enotParser.parse(json, enotContext);
    }

    public List<byte[]> serialize(String json, SerializationContext context) throws EnotParsingException, EnotSerializationException {
        return enotSerializer.serialize(json, context, enotContext);
    }

    public List<byte[]> serialize(EnotElement element, SerializationContext context) throws EnotSerializationException {
        return enotSerializer.serialize(element, context, enotContext);
    }

    public List<byte[]> serialize(List<EnotElement> elements, SerializationContext context) throws EnotSerializationException {
        return enotSerializer.serialize(elements, context, enotContext);
    }

    public String getParamsExampleJson(String json) throws EnotParsingException {
        return objectMapper.writeValueAsString(getParamsExample(json));
    }

    public Map<String, Object> getParamsExample(String json) throws EnotParsingException {
        List<EnotElement> elements = enotParser.parse(json, enotContext);
        return ParamUtils.toMap(exampleParamsExtractor.extractExampleParams(elements));
    }

    public String getParamsExampleJson(EnotElement element) {
        return objectMapper.writeValueAsString(getParamsExample(element));
    }

    public Map<String, Object> getParamsExample(EnotElement element) {
        return ParamUtils.toMap(exampleParamsExtractor.extractExampleParams(Collections.singletonList(element)));
    }

    public String getParamsExampleJson(List<EnotElement> elements) {
        return objectMapper.writeValueAsString(getParamsExample(elements));
    }

    public Map<String, Object> getParamsExample(List<EnotElement> elements) {
        return ParamUtils.toMap(exampleParamsExtractor.extractExampleParams(elements));
    }
}
