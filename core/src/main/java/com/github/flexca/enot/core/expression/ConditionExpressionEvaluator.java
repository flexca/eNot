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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Evaluates a condition expression AST against a {@link SerializationContext},
 * producing a {@code boolean} result.
 *
 * <p>The evaluator walks the {@link ExpressionBlock} tree produced by
 * {@link ConditionExpressionParser} and resolves each leaf value through the
 * provided context. Supported value types and their resolution rules:</p>
 * <ul>
 *   <li><b>PLACEHOLDER</b> – resolved via
 *       {@link SerializationContext#resolvePlaceholderValue(String)}; missing
 *       placeholders resolve to {@code null}.</li>
 *   <li><b>TEXT</b> – returned as a {@link String}.</li>
 *   <li><b>INTEGER</b> – returned as a {@link java.math.BigInteger}.</li>
 *   <li><b>BOOLEAN</b> – returned as a {@link Boolean}.</li>
 *   <li><b>NULL_VALUE</b> – returned as {@code null}.</li>
 * </ul>
 *
 * <h2>Comparison rules</h2>
 * <ul>
 *   <li>{@code ==} and {@code !=} use {@link java.util.Objects#equals} with
 *       numeric normalisation: both sides are converted to
 *       {@link java.math.BigDecimal} before comparison so that, for example,
 *       {@code Integer(5)} and {@code BigInteger(5)} are considered equal.</li>
 *   <li>{@code >}, {@code >=}, {@code <}, {@code <=} require both sides to be
 *       the same type ({@link String}, {@link java.time.ZonedDateTime},
 *       {@link Number}, or {@link Boolean}). Mixed types throw
 *       {@link EnotExpressionEvaluationException}.</li>
 * </ul>
 *
 * <h2>Functions</h2>
 * <p>Function calls are dispatched to
 * {@link com.github.flexca.enot.core.expression.model.ConditionFunction#evaluate}.
 * See that enum for the list of built-in functions ({@code date_time},
 * {@code length}, {@code is_null}).</p>
 *
 * <p>This class is stateless and thread-safe.</p>
 *
 * @see ConditionExpressionParser
 * @see SerializationContext
 */
public class ConditionExpressionEvaluator {

    private final EnotRegistry enotRegistry;
    private final ConditionExpressionParser conditionExpressionParser;

    public ConditionExpressionEvaluator(EnotRegistry enotRegistry, ConditionExpressionParser conditionExpressionParser) {

        this.enotRegistry = enotRegistry;
        this.conditionExpressionParser = conditionExpressionParser;
    }

    /**
     * Parses {@code expression} and evaluates the result against
     * {@code serializationContext}.
     *
     * @param expression         the condition expression string; must not be blank
     * @param serializationContext current serialization context used to resolve
     *                           placeholders
     * @return the boolean result of the expression
     * @throws EnotExpressionEvaluationException if evaluation fails (type
     *         mismatch, non-boolean result, etc.)
     * @throws com.github.flexca.enot.core.exception.EnotInvalidArgumentException
     *         if the expression string is syntactically invalid
     */
    public boolean evaluate(String expression, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        ExpressionBlock block = conditionExpressionParser.parse(expression);
        return evaluate(block, serializationContext);
    }

    /**
     * Evaluates a pre-parsed {@link ExpressionBlock} AST against
     * {@code serializationContext}.
     *
     * <p>Use this overload when the same expression is evaluated repeatedly
     * (e.g. once per LOOP iteration) to avoid re-parsing on every call.</p>
     *
     * @param block              root of the pre-parsed AST
     * @param serializationContext current serialization context
     * @return the boolean result
     * @throws EnotExpressionEvaluationException if the final evaluated value
     *         is not a {@link Boolean}
     */
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
            boolean result = normalizedEquals(leftValue, rightValue);
            return block.isInverted() != result;
        } else if (Operator.NOT_EQUALS_OPERATOR.equals(block.getOperator())) {
            boolean result = !normalizedEquals(leftValue, rightValue);
            return block.isInverted() != result;
        } else if (Operator.GREATER_THAN_OPERATOR.equals(block.getOperator())) {
            return compareTwoValues(block.getOperator(), leftValue, rightValue, block.isInverted());
        } else if (Operator.GREATER_THAN_OR_EQUALS_OPERATOR.equals(block.getOperator())) {
            return compareTwoValues(block.getOperator(), leftValue, rightValue, block.isInverted());
        } else if (Operator.LESS_THAN_OPERATOR.equals(block.getOperator())) {
            return compareTwoValues(block.getOperator(), leftValue, rightValue, block.isInverted());
        } else if (Operator.LESS_THAN_OR_EQUALS_OPERATOR.equals(block.getOperator())) {
            return compareTwoValues(block.getOperator(), leftValue, rightValue, block.isInverted());
        } else {
            throw new EnotExpressionEvaluationException("unsupported comparison operator: " + block.getOperator());
        }
    }

    private Object evaluateBinaryBlock(ExpressionNode block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        if (block.getParts() == null || block.getParts().size() < 2) {
            throw new EnotExpressionEvaluationException("comparison expression block must have at least two parts");
        }

        if (block.getOperator() == null) {
            throw new EnotExpressionEvaluationException("comparison expression block must have operator");
        }

        List<Boolean> arguments = new ArrayList<>();
        for (ExpressionBlock argumentBlock : block.getParts()) {
            Object argument = evaluateBlock(argumentBlock, serializationContext);
            if (argument instanceof Boolean booleanArgument) {
                arguments.add(booleanArgument);
            } else {
                throw new EnotExpressionEvaluationException("for binary block parts must be of type boolean");
            }
        }

        boolean result = arguments.get(0);
        for (int i = 1; i < arguments.size(); i++) {
            if (Operator.AND_OPERATOR.equals(block.getOperator())) {
                result = result && arguments.get(i);
            } else if (Operator.OR_OPERATOR.equals(block.getOperator())) {
                result = result || arguments.get(i);
            } else {
                throw new EnotExpressionEvaluationException("unsupported operator for binary block: " + block.getOperator());
            }
        }
        return result;
    }

    private Object evaluateFunction(ExpressionFunction block, SerializationContext serializationContext) throws EnotExpressionEvaluationException {

        List<Object> arguments = new ArrayList<>();
        for (ExpressionBlock argumentBlock : block.getArguments()) {
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

    private boolean normalizedEquals(Object left, Object right) {
        if (left instanceof Number numberLeft && right instanceof Number numberRight) {
            return numberToBigDecimal(numberLeft).compareTo(numberToBigDecimal(numberRight)) == 0;
        }
        return Objects.equals(left, right);
    }

    private boolean compareTwoValues(Operator operator, Object left, Object right, boolean inverted) throws EnotExpressionEvaluationException {

        int compareResult;
        if ((left instanceof String stringLeft) && (right instanceof String stringRight)) {
            compareResult = stringLeft.compareTo(stringRight);
        } else if ((left instanceof ZonedDateTime datetimeLeft) && (right instanceof ZonedDateTime datetimeRight)) {
            compareResult = datetimeLeft.compareTo(datetimeRight);
        } else if ((left instanceof Number numberLeft) && (right instanceof Number numberRight)) {
            BigDecimal decimalLeft = numberToBigDecimal(numberLeft);
            BigDecimal decimalRight = numberToBigDecimal(numberRight);
            compareResult = decimalLeft.compareTo(decimalRight);
        } else if ((left instanceof Boolean booleanLeft) && (right instanceof Boolean booleanRight)) {
            compareResult = booleanLeft.compareTo(booleanRight);
        } else {
            throw new EnotExpressionEvaluationException("not comparable left  [" + left + "] and right [" + right + "] parts");
        }

        boolean result;
        switch (operator) {
            case GREATER_THAN_OPERATOR:
                result = compareResult > 0;
                break;
            case GREATER_THAN_OR_EQUALS_OPERATOR:
                result = compareResult >= 0;
                break;
            case LESS_THAN_OPERATOR:
                result = compareResult < 0;
                break;
            case LESS_THAN_OR_EQUALS_OPERATOR:
                result = compareResult <= 0;
                break;
            default:
                throw new EnotExpressionEvaluationException("unsupported operator: " + operator);
        }

        return inverted != result;
    }

    private BigDecimal numberToBigDecimal(Number input) {

        if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long) {
            return BigDecimal.valueOf(input.longValue());
        } else if (input instanceof BigInteger bigintInput) {
            return new BigDecimal(bigintInput);
        } else if (input instanceof BigDecimal decimalInput) {
            return decimalInput;
        } else if (input instanceof Float || input instanceof Double) {
            return BigDecimal.valueOf(input.doubleValue());
        } else {
            return BigDecimal.valueOf(input.longValue());
        }
    }
}
