package com.github.flexca.enot.core;

import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.element.EnotElement;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class Enot {

    private final EnotRegistry enotRegistry;
    private final ConditionExpressionParser conditionExpressionParser;
    private final ConditionExpressionEvaluator conditionExpressionEvaluator;
    private final EnotParser enotParser;
    private final EnotSerializer enotSerializer;

    public Enot(EnotRegistry enotRegistry, ObjectMapper objectMapper) {
        this.enotRegistry = enotRegistry;
        conditionExpressionParser = new ConditionExpressionParser();
        conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser);
        enotParser = new EnotParser(this.enotRegistry, conditionExpressionParser, objectMapper);
        enotSerializer = new EnotSerializer(this.enotRegistry, enotParser, conditionExpressionEvaluator);
    }

    public List<EnotElement> parse(String json) throws EnotParsingException {
        return enotParser.parse(json);
    }

    public byte[] serialize(String json, Map<String, Object> values) {
        return null;
    }

    public byte[] serialize(EnotElement element, Map<String, Object> values) {
        return null;
    }

    public byte[] serialize(List<EnotElement> elements, Map<String, Object> values) {
        return null;
    }

    public Map<String, Object> getValuesExample(EnotElement element) {
        return null;
    }

    public Map<String, Object> getValuesExample(List<EnotElement> elements) {
        return null;
    }

}
