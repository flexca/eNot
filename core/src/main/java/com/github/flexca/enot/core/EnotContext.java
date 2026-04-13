package com.github.flexca.enot.core;

import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.registry.EnotRegistry;

public class EnotContext {

    private final EnotRegistry enotRegistry;
    private final ConditionExpressionParser conditionExpressionParser;
    private final ConditionExpressionEvaluator conditionExpressionEvaluator;

    public EnotContext(EnotRegistry enotRegistry, ConditionExpressionParser conditionExpressionParser,
                       ConditionExpressionEvaluator conditionExpressionEvaluator) {
        this.enotRegistry = enotRegistry;
        this.conditionExpressionParser = conditionExpressionParser;
        this.conditionExpressionEvaluator = conditionExpressionEvaluator;
    }

    public EnotRegistry getEnotRegistry() {
        return enotRegistry;
    }

    public ConditionExpressionParser getConditionExpressionParser() {
        return conditionExpressionParser;
    }

    public ConditionExpressionEvaluator getConditionExpressionEvaluator() {
        return conditionExpressionEvaluator;
    }
}
