package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.expression.model.ConditionFunction;
import com.github.flexca.enot.core.expression.model.ExpressionBlock;
import com.github.flexca.enot.core.expression.model.ExpressionFunction;
import com.github.flexca.enot.core.expression.model.ExpressionLeaf;
import com.github.flexca.enot.core.expression.model.ExpressionNode;
import com.github.flexca.enot.core.expression.model.Operator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConditionExpressionParserSuccessCasesTest {

    private ConditionExpressionParser conditionExpressionParser;

    @BeforeEach
    void init() {
        conditionExpressionParser = new ConditionExpressionParser();
    }

    @Test
    void parseSimplePlaceholder() {

        ExpressionBlock actual = conditionExpressionParser.parse("${flag}");

        assertThat(actual).isNotNull();
        assertThat(actual).isInstanceOf(ExpressionLeaf.class);
        ExpressionLeaf expression = (ExpressionLeaf) actual;
        assertThat(expression.isInverted()).isFalse();
        assertThat(expression.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(expression.getValue()).isEqualTo("flag");
    }

    @Test
    void parseSimpleInvertedPlaceholder() {

        ExpressionBlock actual = conditionExpressionParser.parse("!${flag}");

        assertThat(actual).isNotNull();
        assertThat(actual).isInstanceOf(ExpressionLeaf.class);
        ExpressionLeaf expression = (ExpressionLeaf) actual;
        assertThat(expression.isInverted()).isTrue();
        assertThat(expression.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(expression.getValue()).isEqualTo("flag");
    }

    @Test
    void parseDateTimeFunctionComparisonWithGreaterThanOrEquals() {
        
        ExpressionBlock actual = conditionExpressionParser.parse("date_time(${valid_from}) >= date_time('2050-01-01T00-00-00Z')");

        assertThat(actual).isNotNull();
        assertThat(actual).isInstanceOf(ExpressionNode.class);
        ExpressionNode node = (ExpressionNode) actual;
        assertThat(node.isInverted()).isFalse();
        assertThat(node.getOperator()).isEqualTo(Operator.GREATER_THAN_OR_EQUALS_OPERATOR);
        assertThat(node.getParts()).hasSize(2);

        ExpressionBlock left = node.getParts().get(0);
        assertThat(left).isInstanceOf(ExpressionFunction.class);
        ExpressionFunction leftFn = (ExpressionFunction) left;
        assertThat(leftFn.isInverted()).isFalse();
        assertThat(leftFn.getConditionFunction()).isEqualTo(ConditionFunction.DATE_TIME);
        assertThat(leftFn.getArguments()).hasSize(1);
        ExpressionLeaf leftArg = (ExpressionLeaf) leftFn.getArguments().get(0);
        assertThat(leftArg.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(leftArg.getValue()).isEqualTo("valid_from");

        ExpressionBlock right = node.getParts().get(1);
        assertThat(right).isInstanceOf(ExpressionFunction.class);
        ExpressionFunction rightFn = (ExpressionFunction) right;
        assertThat(rightFn.isInverted()).isFalse();
        assertThat(rightFn.getConditionFunction()).isEqualTo(ConditionFunction.DATE_TIME);
        assertThat(rightFn.getArguments()).hasSize(1);
        ExpressionLeaf rightArg = (ExpressionLeaf) rightFn.getArguments().get(0);
        assertThat(rightArg.getValueType()).isEqualTo(CommonEnotValueType.TEXT);
        assertThat(rightArg.getValue()).isEqualTo("2050-01-01T00-00-00Z");
    }

    @Test
    void parsePlaceholderComparisonOrPlaceholderComparison() {

        ExpressionBlock actual = conditionExpressionParser.parse("(${valid_from} >= '2050-01-01T00-00-00Z') || (${valid_from} <= '1970-01-01T00-00-00Z')");

        assertThat(actual).isNotNull();
        assertThat(actual).isInstanceOf(ExpressionNode.class);
        ExpressionNode root = (ExpressionNode) actual;
        assertThat(root.isInverted()).isFalse();
        assertThat(root.getOperator()).isEqualTo(Operator.OR_OPERATOR);
        assertThat(root.getParts()).hasSize(2);

        ExpressionNode leftGroup = (ExpressionNode) root.getParts().get(0);
        assertThat(leftGroup.getOperator()).isEqualTo(Operator.GREATER_THAN_OR_EQUALS_OPERATOR);
        assertThat(leftGroup.getParts()).hasSize(2);
        ExpressionLeaf leftGroupLeft = (ExpressionLeaf) leftGroup.getParts().get(0);
        assertThat(leftGroupLeft.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(leftGroupLeft.getValue()).isEqualTo("valid_from");
        ExpressionLeaf leftGroupRight = (ExpressionLeaf) leftGroup.getParts().get(1);
        assertThat(leftGroupRight.getValueType()).isEqualTo(CommonEnotValueType.TEXT);
        assertThat(leftGroupRight.getValue()).isEqualTo("2050-01-01T00-00-00Z");

        ExpressionNode rightGroup = (ExpressionNode) root.getParts().get(1);
        assertThat(rightGroup.getOperator()).isEqualTo(Operator.LESS_THAN_OR_EQUALS_OPERATOR);
        assertThat(rightGroup.getParts()).hasSize(2);
        ExpressionLeaf rightGroupLeft = (ExpressionLeaf) rightGroup.getParts().get(0);
        assertThat(rightGroupLeft.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(rightGroupLeft.getValue()).isEqualTo("valid_from");
        ExpressionLeaf rightGroupRight = (ExpressionLeaf) rightGroup.getParts().get(1);
        assertThat(rightGroupRight.getValueType()).isEqualTo(CommonEnotValueType.TEXT);
        assertThat(rightGroupRight.getValue()).isEqualTo("1970-01-01T00-00-00Z");
    }

    @Test
    void parseCompoundOrGroupAndComparisonWithNotEquals() {

        ExpressionBlock actual = conditionExpressionParser.parse("((${valid_from} >= '2050-01-01T00-00-00Z') || (${valid_from} <= '1970-01-01T00-00-00Z')) && (${valid_to} != '2020-12-12T00-00-00Z')");

        assertThat(actual).isNotNull();
        assertThat(actual).isInstanceOf(ExpressionNode.class);
        ExpressionNode root = (ExpressionNode) actual;
        assertThat(root.isInverted()).isFalse();
        assertThat(root.getOperator()).isEqualTo(Operator.AND_OPERATOR);
        assertThat(root.getParts()).hasSize(2);

        ExpressionNode orGroup = (ExpressionNode) root.getParts().get(0);
        assertThat(orGroup.getOperator()).isEqualTo(Operator.OR_OPERATOR);
        assertThat(orGroup.getParts()).hasSize(2);
        ExpressionNode orLeft = (ExpressionNode) orGroup.getParts().get(0);
        assertThat(orLeft.getOperator()).isEqualTo(Operator.GREATER_THAN_OR_EQUALS_OPERATOR);
        ExpressionLeaf orLeftLeft = (ExpressionLeaf) orLeft.getParts().get(0);
        assertThat(orLeftLeft.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(orLeftLeft.getValue()).isEqualTo("valid_from");
        ExpressionLeaf orLeftRight = (ExpressionLeaf) orLeft.getParts().get(1);
        assertThat(orLeftRight.getValueType()).isEqualTo(CommonEnotValueType.TEXT);
        assertThat(orLeftRight.getValue()).isEqualTo("2050-01-01T00-00-00Z");

        ExpressionNode orRight = (ExpressionNode) orGroup.getParts().get(1);
        assertThat(orRight.getOperator()).isEqualTo(Operator.LESS_THAN_OR_EQUALS_OPERATOR);
        ExpressionLeaf orRightLeft = (ExpressionLeaf) orRight.getParts().get(0);
        assertThat(orRightLeft.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(orRightLeft.getValue()).isEqualTo("valid_from");
        ExpressionLeaf orRightRight = (ExpressionLeaf) orRight.getParts().get(1);
        assertThat(orRightRight.getValueType()).isEqualTo(CommonEnotValueType.TEXT);
        assertThat(orRightRight.getValue()).isEqualTo("1970-01-01T00-00-00Z");

        ExpressionNode andRight = (ExpressionNode) root.getParts().get(1);
        assertThat(andRight.getOperator()).isEqualTo(Operator.NOT_EQUALS_OPERATOR);
        assertThat(andRight.getParts()).hasSize(2);
        ExpressionLeaf andRightLeft = (ExpressionLeaf) andRight.getParts().get(0);
        assertThat(andRightLeft.getValueType()).isEqualTo(CommonEnotValueType.PLACEHOLDER);
        assertThat(andRightLeft.getValue()).isEqualTo("valid_to");
        ExpressionLeaf andRightRight = (ExpressionLeaf) andRight.getParts().get(1);
        assertThat(andRightRight.getValueType()).isEqualTo(CommonEnotValueType.TEXT);
        assertThat(andRightRight.getValue()).isEqualTo("2020-12-12T00-00-00Z");
    }
}
