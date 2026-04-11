package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotExpressionEvaluationException;
import com.github.flexca.enot.core.expression.model.ExpressionBlock;
import com.github.flexca.enot.core.expression.model.ExpressionFunction;
import com.github.flexca.enot.core.expression.model.ExpressionLeaf;
import com.github.flexca.enot.core.expression.model.ExpressionNode;
import com.github.flexca.enot.core.expression.model.Operator;
import com.github.flexca.enot.core.expression.model.OperatorType;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.context.SerializationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConditionExpressionEvaluator {

    private final EnotRegistry enotRegistry;
    private final ConditionExpressionParser conditionExpressionParser;

    public ConditionExpressionEvaluator(EnotRegistry enotRegistry, ConditionExpressionParser conditionExpressionParser) {

        this.enotRegistry = enotRegistry;
        this.conditionExpressionParser = conditionExpressionParser;
    }

    public boolean evaluate(String expression, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        ExpressionBlock block = conditionExpressionParser.parse(expression);
        return evaluate(block, serializationContext);
    }

    public boolean evaluate(ExpressionBlock block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        Object result = evaluateBlock(block, serializationContext);
        if (result instanceof Boolean booleanResul) {
            return booleanResul;
        }
        throw new EnotExpressionEvaluationException("expression evaluation result must be a boolean value");
    }

    private Object evaluateBlock(ExpressionBlock block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        if (block == null) {
            throw new EnotExpressionEvaluationException("expression block must not be null");
        }

        if (block instanceof ExpressionNode nodeBlock) {
            if (nodeBlock.getOperator() == null) {
                throw new EnotExpressionEvaluationException("operator must not be null");
            }
            if (OperatorType.COMPARISON.equals(nodeBlock.getOperator().getType())) {
                return evaluateComparisonBlock(nodeBlock, serializationContext);
            } else if (OperatorType.BINARY.equals(nodeBlock.getOperator().getType())) {
                return evaluateBinaryBlock(nodeBlock, serializationContext);
            } else {
                throw new EnotExpressionEvaluationException("unsupported operator type: " + nodeBlock.getOperator().getType());
            }
        } else if (block instanceof ExpressionFunction functionBlock) {
            return evaluateFunction(functionBlock, serializationContext);
        } else if (block instanceof ExpressionLeaf leafBlock) {
            return evaluatePrimitiveValue(leafBlock, serializationContext);
        } else {
            throw new EnotExpressionEvaluationException("unsupported expression block: " + block.getClass());
        }
    }

    private Object evaluateComparisonBlock(ExpressionNode block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        if (block.getParts() == null || block.getParts().size() != 2) {
            throw new EnotExpressionEvaluationException("comparison expression block must have both left and right parts");
        }

        if (block.getOperator() == null) {
            throw new EnotExpressionEvaluationException("comparison expression block must have operator");
        }

        ExpressionBlock leftBlock = block.getParts().get(0);
        ExpressionBlock rightBlock = block.getParts().get(1);

        Object leftValue = evaluateBlock(leftBlock, serializationContext);
        Object rightValue = evaluateBlock(rightBlock, serializationContext);

        if (Operator.EQUALS_OPERATOR.equals(block.getOperator())) {
            boolean result = Objects.equals(leftValue, rightValue);
            return block.isInverted() != result;
        } else if (Operator.NOT_EQUALS_OPERATOR.equals(block.getOperator())) {
            boolean result = !Objects.equals(leftValue, rightValue);
            return block.isInverted() != result;
        } else if (Operator.GREATER_THAN_OPERATOR.equals(block.getOperator())) {
            return false;
        } else if (Operator.GREATER_THAN_OR_EQUALS_OPERATOR.equals(block.getOperator())) {
            return false;
        } else if (Operator.LESS_THAN_OPERATOR.equals(block.getOperator())) {
            return false;
        } else if (Operator.LESS_THAN_OR_EQUALS_OPERATOR.equals(block.getOperator())) {
            return false;
        } else {
            throw new EnotExpressionEvaluationException("unsupported comparison operator: " + block.getOperator());
        }
    }

    private Object evaluateBinaryBlock(ExpressionNode block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {
        return null;
    }

    private Object evaluateFunction(ExpressionFunction block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        List<Object> arguments = new ArrayList<>();
        for(ExpressionBlock argumentBlock : block.getArguments()) {
            Object argument = evaluateBlock(argumentBlock, serializationContext);
            arguments.add(argument);
        }
        return block.getConditionFunction().evaluate(arguments);
    }

    private Object evaluatePrimitiveValue(ExpressionLeaf block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        Object primitiveValue;
        if (CommonEnotValueType.PLACEHOLDER.equals(block.getValueType())) {
            if (block.getValue() instanceof String placeholderName) {
                primitiveValue = serializationContext.resolvePlaceholderValue(placeholderName);
            } else {
                throw new EnotExpressionEvaluationException("placeholder name must be a string");
            }
        } else {
            primitiveValue = block.getValue();
        }

        if (block.isInverted()) {
            if (primitiveValue instanceof Boolean booleanValue) {
                return !booleanValue;
            } else {
                throw new EnotExpressionEvaluationException("non boolean values cannot be inverted");
            }
        }

        return primitiveValue;
    }


}
