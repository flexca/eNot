package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.expression.model.ConditionFunction;
import com.github.flexca.enot.core.expression.model.ExpressionBlock;
import com.github.flexca.enot.core.expression.model.ExpressionNode;
import com.github.flexca.enot.core.expression.model.Operator;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConditionExpressionParser {

    private static final String BLOCK_NAME_TEMPLATE = "#block%d";
    private static final char OPENING_BRACKET = '(';
    private static final char CLOSING_BRACKET = ')';
    private static final char LITERAL = '\'';

    public ExpressionBlock parse(String expression) {

        if (StringUtils.isBlank(expression)) {
            throw new EnotInvalidArgumentException("expression must not be blank");
        }

        validateBracketsAndLiterals(expression);
        String expressionWithoutSpaces = removeSpaces(collapseInversions(expression));

        return parseExpression(expressionWithoutSpaces);
    }



    private ExpressionBlock parseExpression(String expression) {

        Map<String, ExpressionBlock> blocks = new HashMap<>();
        return collapseBlocks(expression, blocks);
    }

    private ExpressionBlock collapseBlocks(String expression, Map<String, ExpressionBlock> blocks) {

        StringBuilder expressionLeftover;
        String currentExpression = expression;
        int innerBlockOpeningIndex;

        while ((innerBlockOpeningIndex = currentExpression.lastIndexOf(OPENING_BRACKET)) >= 0) {

            int innerBlockClosingIndex = currentExpression.indexOf(CLOSING_BRACKET, innerBlockOpeningIndex + 1);
            if (innerBlockClosingIndex < 0) {
                throw new EnotInvalidArgumentException("missing closing bracket");
            }

            FunctionExtractionResult functionExtractionResult = getFunctionWithIndex(currentExpression, innerBlockOpeningIndex);
            String subExpression = currentExpression.substring(innerBlockOpeningIndex + 1, innerBlockClosingIndex).trim();
            ExpressionBlock block;
            if (functionExtractionResult != null) {
                block = parseFunctionExpression(subExpression, blocks, functionExtractionResult);
            } else {
                block = parseBinaryExpression(subExpression, blocks);
            }

            String blockName = String.format(BLOCK_NAME_TEMPLATE, (blocks.size() + 1));
            blocks.put(blockName, block);

            expressionLeftover = new StringBuilder();
            if (innerBlockOpeningIndex > 0) {
                expressionLeftover.append(currentExpression.substring(0, innerBlockOpeningIndex));
            }
            expressionLeftover.append(blockName);
            if (innerBlockClosingIndex + 1 < currentExpression.length() - 1) {
                expressionLeftover.append(currentExpression.substring(innerBlockClosingIndex + 1));
            }
            currentExpression = expressionLeftover.toString();
        }

        return parseBinaryExpression(currentExpression, blocks);

    }

    private ExpressionBlock parseFunctionExpression(String subExpression, Map<String, ExpressionBlock> blocks,
                                                    FunctionExtractionResult functionExtractionResult) {

        return null;
    }

    private FunctionExtractionResult getFunctionWithIndex(String currentExpression, int blockOpeningIndex) {

        int breakIndex = 0;
        for (int i = blockOpeningIndex - 1; i >= 0; i--) {
            char c = currentExpression.charAt(i);
            if (c == OPENING_BRACKET || c == '|' || c == '&' || c == '=' /*|| c == '!'*/ || c == '>' || c == '<' || c == ',') {
                breakIndex = i;
                break;
            }
        }

        if (blockOpeningIndex - breakIndex <= 0) {
            return null;
        }

        String functionCandidateName = currentExpression.substring(breakIndex, blockOpeningIndex).trim();
        boolean inverted = false;
        if (functionCandidateName.startsWith(Operator.NOT_OPERATOR.getOperator())) {
            if (blockOpeningIndex - breakIndex == 1) {
                return null;
            }
            inverted = true;
            functionCandidateName = functionCandidateName.substring(1);
        }
        ConditionFunction conditionFunction = ConditionFunction.getByName(functionCandidateName);
        if (conditionFunction == null) {
            throw new EnotInvalidArgumentException("unsupported function: " + functionCandidateName);
        }

        return new FunctionExtractionResult(conditionFunction, breakIndex + 1, inverted);
    }

    private ExpressionBlock parseBinaryExpression(String expression, Map<String, ExpressionBlock> blocks) {

        char previousChar = expression.charAt(0);
        int lastOperatorIndex = -1;
        List<String> parts = new ArrayList<>();
        List<Operator> operators = new ArrayList<>();

        boolean insideLiteral = previousChar == LITERAL;
        for (int i = 1; i < expression.length(); i++) {

            char currentChar = expression.charAt(i);

            if (currentChar == LITERAL) {
                insideLiteral = !insideLiteral;
            }

            Operator currentOperator = null;
            if (!insideLiteral) {
                if (previousChar == '|' && currentChar == '|') {
                    currentOperator = Operator.OR_OPERATOR;
                }
                if (previousChar == '&' && currentChar == '&') {
                    currentOperator = Operator.AND_OPERATOR;
                }
            }

            if (currentOperator != null) {
                String part = expression.substring(lastOperatorIndex + 1, i - 1);
                lastOperatorIndex = i;
                parts.add(part);
                operators.add(currentOperator);
            }

            if (i == expression.length() - 1) {
                String part = expression.substring(lastOperatorIndex + 1);
                parts.add(part);
            }

            previousChar = currentChar;
        }

        if (operators.contains(Operator.OR_OPERATOR) && operators.contains(Operator.AND_OPERATOR)) {
            throw new EnotInvalidArgumentException("both OR (||) and AND (&&) operators have same priority. Thus usage of expressions like " +
                    "'A || B && C' is not allowed as priority is not clear, add additional brackets to emphasize the priority, " +
                    "examples: 'A || (B && C)' or '(A || B) && C'");
        }

        if (parts.size() - 1 > operators.size()) {
            throw new EnotInvalidArgumentException("missing operators in expressions");
        }
        if (parts.size() - 1 < operators.size()) {
            throw new EnotInvalidArgumentException("extra operators in expressions");
        }

        for (String part : parts) {

        }

        return null;
    }

    private ExpressionBlock parseComparisonExpression(String expression) {

        Operator operator = null;
        String[] expressionParts = null;
        for (Operator candidate : Operator.getComparisonOperators()) {
            String[] parts = expression.split(candidate.getOperator());
            if (parts.length == 2) {
                if (operator != null) {
                    Operator compatible = resolveCompatible(operator, candidate);
                    if (compatible == null) {
                        throw new EnotInvalidArgumentException("unexpected multiple comparison operators: " + operator.getOperator()
                                + " and " + candidate.getOperator() + " for expression [" + expression + "], expecting only one operator");
                    } else if (compatible.equals(operator)) {
                        continue;
                    }
                }
                operator = candidate;
                expressionParts = parts;
            } else if (parts.length != 1) {
                throw new EnotInvalidArgumentException("unexpected multiple comparison operators: " + candidate.getOperator()
                        + " in expression " + expression + ", expecting only one operator");
            }
        }

        if (operator == null) {
            operator = Operator.EQUALS_OPERATOR;
            expressionParts = new String[2];
            expressionParts[0] = expression;
            expressionParts[1] = "true";
        }

        String left = expressionParts[0];
        String right = expressionParts[1];

        ExpressionBlock leftBlock = extractBlock(left, "left", expression);
        ExpressionBlock rightBlock = extractBlock(right, "right", expression);

        return new ExpressionNode(false, List.of(leftBlock, rightBlock), operator);
    }

    private Operator resolveCompatible(Operator first, Operator second) {
        Set<Operator> operators = Set.of(first, second);
        if (operators.contains(Operator.GREATER_THAN_OPERATOR) && operators.contains(Operator.GREATER_THAN_OR_EQUALS_OPERATOR)) {
            return Operator.GREATER_THAN_OR_EQUALS_OPERATOR;
        }
        if (operators.contains(Operator.LESS_THAN_OPERATOR) && operators.contains(Operator.LESS_THAN_OR_EQUALS_OPERATOR)) {
            return Operator.LESS_THAN_OR_EQUALS_OPERATOR;
        }
        return null;
    }

    private ExpressionBlock extractBlock(String expressionPart, String partName, String parentExpression) {

        if (StringUtils.isBlank(expressionPart)) {
            throw new EnotInvalidArgumentException("Blank " + partName + " part of expression: " + parentExpression);
        }

        String expressionPartTrimmed = expressionPart.trim();

        PlaceholderUtils.isPlaceholder(expressionPartTrimmed);

        return null;
    }


    private void validateBracketsAndLiterals(String expression) {

        boolean insideLiteral = false;
        int openingBracketCounter = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == LITERAL) {
                insideLiteral = !insideLiteral;
            }
            if (!insideLiteral) {
                if (c == OPENING_BRACKET) {
                    openingBracketCounter++;
                }
                if (c == CLOSING_BRACKET) {
                    openingBracketCounter--;
                }
            }
            if (openingBracketCounter < 0) {
                throw new EnotInvalidArgumentException("extra closing bracket in expression");
            }
        }
        if (openingBracketCounter > 0) {
            throw new EnotInvalidArgumentException("missing closing bracket in expression");
        }
        if (insideLiteral) {
            throw new EnotInvalidArgumentException("not closed literal in expression");
        }
    }

    private String removeSpaces(String input) {
        StringBuilder result = new StringBuilder();
        boolean insideLiteral = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == LITERAL) {
                insideLiteral = !insideLiteral;
            }
            if (!insideLiteral && c != ' ' && c != '\n' && c != '\t' && c != '\r') {
                result.append(c);
            }
        }
        return result.toString();
    }

    private class FunctionExtractionResult {

        private final ConditionFunction conditionFunction;
        private final int startIndex;
        private final boolean inverted;

        public FunctionExtractionResult(ConditionFunction conditionFunction, int startIndex, boolean inverted) {
            this.conditionFunction = conditionFunction;
            this.startIndex = startIndex;
            this.inverted = inverted;
        }

        public ConditionFunction getConditionFunction() {
            return conditionFunction;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public boolean isInverted() {
            return inverted;
        }
    }

    private String collapseInversions(String expression) {
        return null;
    }
}
