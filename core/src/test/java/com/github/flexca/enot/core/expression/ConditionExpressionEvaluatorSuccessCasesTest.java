package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConditionExpressionEvaluatorSuccessCasesTest {

    private EnotRegistry enotRegistry;
    private ConditionExpressionParser conditionExpressionParser;

    private ConditionExpressionEvaluator conditionExpressionEvaluator;

    @BeforeEach
    void init() {
        enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .build();
        conditionExpressionParser = new ConditionExpressionParser();

        conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser);
    }

    @Test
    void testEvaluate() {

    }
}
