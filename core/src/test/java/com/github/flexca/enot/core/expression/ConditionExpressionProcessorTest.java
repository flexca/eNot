package com.github.flexca.enot.core.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionExpressionProcessorTest {

    private ConditionExpressionProcessor conditionExpressionProcessor;

    @BeforeEach
    void init() {
        conditionExpressionProcessor = new ConditionExpressionProcessor();
    }

    @Test
    void testParseExpression() {

        conditionExpressionProcessor.parse("date_time(${valid_from}) >= date_time('2050-01-01T00-00-00Z')");

        assertThat(true).isTrue();
    }

    @Test
    void testParseExpression2() {

        conditionExpressionProcessor.parse("(${valid_from} >= '2050-01-01T00-00-00Z') || (${valid_from} <= '1970-01-01T00-00-00Z')");

        assertThat(true).isTrue();
    }

    @Test
    void testParseExpression3() {

        conditionExpressionProcessor.parse("((${valid_from} >= '2050-01-01T00-00-00Z') || (${valid_from} <= '1970-01-01T00-00-00Z')) && (${valid_to} != '2020-12-12T00-00-00Z')");

        assertThat(true).isTrue();
    }
}
