package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.expression.model.ExpressionBlock;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.SerializationContext;

public class ConditionExpressionEvaluator {

    private final EnotRegistry enotRegistry;
    private final ConditionExpressionParser conditionExpressionParser;

    public ConditionExpressionEvaluator(EnotRegistry enotRegistry, ConditionExpressionParser conditionExpressionParser) {

        this.enotRegistry = enotRegistry;
        this.conditionExpressionParser = conditionExpressionParser;
    }

    public boolean evaluate(String expression, SerializationContext serializationContext) {

        ExpressionBlock block = conditionExpressionParser.parse(expression);
        return evaluate(block, serializationContext);
    }

    public boolean evaluate(ExpressionBlock block, SerializationContext serializationContext) {

        return false;
    }
}
