package com.github.flexca.enot.core;

import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;

public class EnotContext {

    private final EnotRegistry enotRegistry;
    private final EnotParser enotParser;
    private final EnotSerializer enotSerializer;
    private final ConditionExpressionParser conditionExpressionParser;
    private final ConditionExpressionEvaluator conditionExpressionEvaluator;

    public EnotContext(EnotRegistry enotRegistry, EnotParser enotParser, EnotSerializer enotSerializer,
                       ConditionExpressionParser conditionExpressionParser, ConditionExpressionEvaluator conditionExpressionEvaluator) {
        this.enotRegistry = enotRegistry;
        this.enotParser = enotParser;
        this.enotSerializer = enotSerializer;
        this.conditionExpressionParser = conditionExpressionParser;
        this.conditionExpressionEvaluator = conditionExpressionEvaluator;
    }

    public EnotRegistry getEnotRegistry() {
        return enotRegistry;
    }

    public EnotParser getEnotParser() {
        return enotParser;
    }

    public EnotSerializer getEnotSerializer() {
        return enotSerializer;
    }

    public ConditionExpressionParser getConditionExpressionParser() {
        return conditionExpressionParser;
    }

    public ConditionExpressionEvaluator getConditionExpressionEvaluator() {
        return conditionExpressionEvaluator;
    }
}
