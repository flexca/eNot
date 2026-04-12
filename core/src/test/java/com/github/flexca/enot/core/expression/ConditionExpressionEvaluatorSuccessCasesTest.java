package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionExpressionEvaluatorSuccessCasesTest {

    private ObjectMapper objectMapper;
    private ConditionExpressionEvaluator evaluator;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .build();
        ConditionExpressionParser parser = new ConditionExpressionParser();
        evaluator = new ConditionExpressionEvaluator(enotRegistry, parser);
    }

    private SerializationContext ctx() {
        return new SerializationContext.Builder(objectMapper).build();
    }

    private SerializationContext ctx(Map<String, Object> params) {
        return new SerializationContext.Builder(objectMapper).withParams(params).build();
    }

    // -----------------------------------------------------------------------
    // Boolean literal
    // -----------------------------------------------------------------------

    @Test
    void evaluateTrueLiteral() throws Exception {
        assertThat(evaluator.evaluate("true", ctx())).isTrue();
    }

    @Test
    void evaluateFalseLiteral() throws Exception {
        assertThat(evaluator.evaluate("false", ctx())).isFalse();
    }

    @Test
    void evaluateInvertedTrueLiteralReturnsFalse() throws Exception {
        assertThat(evaluator.evaluate("!true", ctx())).isFalse();
    }

    @Test
    void evaluateInvertedFalseLiteralReturnsTrue() throws Exception {
        assertThat(evaluator.evaluate("!false", ctx())).isTrue();
    }

    // -----------------------------------------------------------------------
    // Placeholder resolution
    // -----------------------------------------------------------------------

    @Test
    void evaluatePlaceholderEqualsText() throws Exception {
        assertThat(evaluator.evaluate("${name} == 'Alice'", ctx(Map.of("name", "Alice")))).isTrue();
    }

    @Test
    void evaluatePlaceholderNotEqualsText() throws Exception {
        assertThat(evaluator.evaluate("${name} != 'Bob'", ctx(Map.of("name", "Alice")))).isTrue();
    }

    @Test
    void evaluatePlaceholderEqualsTextFalseWhenMismatch() throws Exception {
        assertThat(evaluator.evaluate("${name} == 'Bob'", ctx(Map.of("name", "Alice")))).isFalse();
    }

    @Test
    void evaluateMissingPlaceholderEqualsNull() throws Exception {
        assertThat(evaluator.evaluate("${missing} == null", ctx())).isTrue();
    }

    // -----------------------------------------------------------------------
    // Global params
    // -----------------------------------------------------------------------

    @Test
    void evaluateGlobalPlaceholderEquals() throws Exception {
        SerializationContext ctx = new SerializationContext.Builder(objectMapper)
                .withGlobalParam("env", "prod")
                .build();
        assertThat(evaluator.evaluate("${global.env} == 'prod'", ctx)).isTrue();
    }

    // -----------------------------------------------------------------------
    // Integer comparisons
    // -----------------------------------------------------------------------

    @Test
    void evaluateIntegerGreaterThan() throws Exception {
        assertThat(evaluator.evaluate("${age} > 18", ctx(Map.of("age", 30)))).isTrue();
    }

    @Test
    void evaluateIntegerGreaterThanOrEquals() throws Exception {
        assertThat(evaluator.evaluate("${age} >= 18", ctx(Map.of("age", 18)))).isTrue();
    }

    @Test
    void evaluateIntegerLessThan() throws Exception {
        assertThat(evaluator.evaluate("${score} < 10", ctx(Map.of("score", 5)))).isTrue();
    }

    @Test
    void evaluateIntegerLessThanOrEquals() throws Exception {
        assertThat(evaluator.evaluate("${score} <= 10", ctx(Map.of("score", 10)))).isTrue();
    }

    @Test
    void evaluateIntegerGreaterThanFalse() throws Exception {
        assertThat(evaluator.evaluate("${age} > 18", ctx(Map.of("age", 10)))).isFalse();
    }

    // -----------------------------------------------------------------------
    // date_time function
    // -----------------------------------------------------------------------

    @Test
    void evaluateDateTimeGreaterThanOrEquals() throws Exception {
        assertThat(evaluator.evaluate(
                "date_time(${not_after}) >= date_time('2025-01-01T00:00:00Z')",
                ctx(Map.of("not_after", "2050-01-01T00:00:00Z")))).isTrue();
    }

    @Test
    void evaluateDateTimeLessThan() throws Exception {
        assertThat(evaluator.evaluate(
                "date_time(${not_after}) < date_time('2025-01-01T00:00:00Z')",
                ctx(Map.of("not_after", "2020-01-01T00:00:00Z")))).isTrue();
    }

    @Test
    void evaluateDateTimeEquals() throws Exception {
        assertThat(evaluator.evaluate(
                "date_time(${not_after}) == date_time('2050-01-01T00:00:00Z')",
                ctx(Map.of("not_after", "2050-01-01T00:00:00Z")))).isTrue();
    }

    // -----------------------------------------------------------------------
    // length function
    // -----------------------------------------------------------------------

    @Test
    void evaluateLengthGreaterThan() throws Exception {
        assertThat(evaluator.evaluate("length(${cn}) > 3", ctx(Map.of("cn", "Alice")))).isTrue();
    }

    @Test
    void evaluateLengthEquals() throws Exception {
        assertThat(evaluator.evaluate("length(${cn}) == 5", ctx(Map.of("cn", "Alice")))).isTrue();
    }

    @Test
    void evaluateLengthOnNullReturnsZero() throws Exception {
        assertThat(evaluator.evaluate("length(${missing}) == 0", ctx())).isTrue();
    }

    // -----------------------------------------------------------------------
    // is_null function
    // -----------------------------------------------------------------------

    @Test
    void evaluateIsNullOnMissingPlaceholder() throws Exception {
        assertThat(evaluator.evaluate("is_null(${missing}) == true", ctx())).isTrue();
    }

    @Test
    void evaluateIsNullOnPresentPlaceholder() throws Exception {
        assertThat(evaluator.evaluate("is_null(${name}) == false", ctx(Map.of("name", "Alice")))).isTrue();
    }

    // -----------------------------------------------------------------------
    // Binary logical operators
    // -----------------------------------------------------------------------

    @Test
    void evaluateAndBothTrue() throws Exception {
        assertThat(evaluator.evaluate("(${a} == 'x') && (${b} == 'y')", ctx(Map.of("a", "x", "b", "y")))).isTrue();
    }

    @Test
    void evaluateAndOneFalseReturnsFalse() throws Exception {
        assertThat(evaluator.evaluate("(${a} == 'x') && (${b} == 'y')", ctx(Map.of("a", "x", "b", "z")))).isFalse();
    }

    @Test
    void evaluateOrOneTrue() throws Exception {
        assertThat(evaluator.evaluate("(${a} == 'x') || (${b} == 'y')", ctx(Map.of("a", "x", "b", "z")))).isTrue();
    }

    @Test
    void evaluateOrBothFalseReturnsFalse() throws Exception {
        assertThat(evaluator.evaluate("(${a} == 'x') || (${b} == 'y')", ctx(Map.of("a", "a", "b", "b")))).isFalse();
    }

    @Test
    void evaluateThreeWayAndChain() throws Exception {
        assertThat(evaluator.evaluate(
                "(${a} == 'x') && (${b} == 'y') && (${c} == 'z')",
                ctx(Map.of("a", "x", "b", "y", "c", "z")))).isTrue();
    }

    // -----------------------------------------------------------------------
    // Inverted groups
    // -----------------------------------------------------------------------

    @Test
    void evaluateInvertedGroupNegatesResult() throws Exception {
        assertThat(evaluator.evaluate("!(${name} == 'Bob')", ctx(Map.of("name", "Alice")))).isTrue();
    }

    @Test
    void evaluateInvertedComparisonReturnsFalse() throws Exception {
        assertThat(evaluator.evaluate("!(${name} == 'Alice')", ctx(Map.of("name", "Alice")))).isFalse();
    }

    // -----------------------------------------------------------------------
    // Nested path placeholder resolution
    // -----------------------------------------------------------------------

    @Test
    void evaluatePlaceholderAfterPathStepForward() throws Exception {
        SerializationContext ctx = ctx(Map.of("subject_dn", Map.of("common_name", "Alice")));
        ctx.pathStepForward("subject_dn");
        assertThat(evaluator.evaluate("${common_name} == 'Alice'", ctx)).isTrue();
        ctx.pathStepBack();
    }

    // -----------------------------------------------------------------------
    // Collection length
    // -----------------------------------------------------------------------

    @Test
    void evaluateLengthOnCollection() throws Exception {
        assertThat(evaluator.evaluate("length(${items}) == 3", ctx(Map.of("items", List.of("a", "b", "c"))))).isTrue();
    }
}

