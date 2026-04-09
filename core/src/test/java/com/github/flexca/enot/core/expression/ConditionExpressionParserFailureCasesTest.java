package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConditionExpressionParserFailureCasesTest {

    private ConditionExpressionParser conditionExpressionParser;

    @BeforeEach
    void init() {
        conditionExpressionParser = new ConditionExpressionParser();
    }

    @Test
    void blankExpressionThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("   "))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void nullExpressionThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse(null))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void unclosedBracketThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("(${flag} == true"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void extraClosingBracketThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("${flag} == true)"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void unclosedLiteralThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("${flag} == '2050-01-01"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void unknownFunctionThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("unknown_fn(${flag}) == true"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void mixedOrAndAndWithoutBracketsThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("(${a} == true) || (${b} == true) && (${c} == true)"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void wrongArgumentCountForFunctionThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("date_time(${a}, ${b}) >= date_time('2050-01-01T00-00-00Z')"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void extraComparisonOperatorThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("${flag} == true == false"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void undefinedSymbolThrows() {
        assertThatThrownBy(() -> conditionExpressionParser.parse("undefinedToken"))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }
}
