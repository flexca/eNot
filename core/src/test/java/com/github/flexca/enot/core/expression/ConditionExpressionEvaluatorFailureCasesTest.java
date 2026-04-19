package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.exception.EnotExpressionEvaluationException;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConditionExpressionEvaluatorFailureCasesTest {

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    private ConditionExpressionEvaluator evaluator;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .build();
        ConditionExpressionParser parser = new ConditionExpressionParser();
        evaluator = new ConditionExpressionEvaluator(enotRegistry, parser);
    }

    private SerializationContext ctx() {
        return new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .build();
    }

    private SerializationContext ctx(Map<String, Object> params) {
        return new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
    }

    // -----------------------------------------------------------------------
    // Parse-time failures (invalid expressions)
    // -----------------------------------------------------------------------

    @Test
    void evaluateBlankExpressionThrows() {
        assertThatThrownBy(() -> evaluator.evaluate("   ", ctx()))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void evaluateNullExpressionThrows() {
        assertThatThrownBy(() -> evaluator.evaluate((String) null, ctx()))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void evaluateUnknownFunctionThrows() {
        assertThatThrownBy(() -> evaluator.evaluate("unknown_fn(${x}) == 'a'", ctx()))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void evaluateMixedAndOrWithoutBracketsThrows() {
        assertThatThrownBy(() -> evaluator.evaluate("${a} == 'x' || ${b} == 'y' && ${c} == 'z'", ctx()))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // Evaluation-time failures (type mismatches, incompatible comparisons)
    // -----------------------------------------------------------------------

    @Test
    void evaluateInvertedNonBooleanPlaceholderThrows() {
        assertThatThrownBy(() -> evaluator.evaluate("!${name}", ctx(Map.of("name", "Alice"))))
                .isInstanceOf(EnotExpressionEvaluationException.class);
    }

    @Test
    void evaluateCompareStringWithIntegerThrows() {
        assertThatThrownBy(() -> evaluator.evaluate("${name} > 5", ctx(Map.of("name", "Alice"))))
                .isInstanceOf(EnotExpressionEvaluationException.class);
    }

    @Test
    void evaluateCompareDateTimeWithStringThrows() {
        assertThatThrownBy(() -> evaluator.evaluate(
                "date_time(${not_after}) > 'some_string'",
                ctx(Map.of("not_after", "2050-01-01T00:00:00Z"))))
                .isInstanceOf(EnotExpressionEvaluationException.class);
    }

    @Test
    void evaluateInvalidDateTimeFormatThrows() {
        assertThatThrownBy(() -> evaluator.evaluate(
                "date_time(${not_after}) > date_time('2025-01-01T00:00:00Z')",
                ctx(Map.of("not_after", "not-a-date"))))
                .isInstanceOf(EnotExpressionEvaluationException.class);
    }

    @Test
    void evaluateLengthOnNonCollectionTypeThrows() {
        assertThatThrownBy(() -> evaluator.evaluate("length(${count}) > 0", ctx(Map.of("count", 42))))
                .isInstanceOf(EnotExpressionEvaluationException.class);
    }

    @Test
    void evaluateResultIsNotBooleanThrows() {
        // A plain text placeholder resolves to a string вЂ” not a valid boolean top-level result
        assertThatThrownBy(() -> evaluator.evaluate("${name}", ctx(Map.of("name", "Alice"))))
                .isInstanceOf(EnotExpressionEvaluationException.class);
    }
}
