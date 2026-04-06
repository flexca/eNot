package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConditionExpressionProcessor {

    private static final String BLOCK_NAME_TEMPLATE = "#block%d";
    private static final char OPENING_BRACKET = '(';
    private static final char CLOSING_BRACKET = ')';

    public void parse(String expression) {

        if (StringUtils.isBlank(expression)) {
            throw new EnotInvalidArgumentException("expression must not be blank");
        }

        validateBrackets(expression);
        String expressionWithoutSpaces = removeSpaces(expression);

        parseExpression(expressionWithoutSpaces);

    }

    private void parseExpression(String expression) {

        String collapsedExpression = collapseBlocks(expression, new HashMap<>());
    }

    private String collapseBlocks(String expression, Map<String, String> blocks) {

        StringBuilder expressionLeftover;
        String currentExpression = expression;
        int innerBlockOpeningIndex;

        while ((innerBlockOpeningIndex = currentExpression.lastIndexOf(OPENING_BRACKET)) >= 0) {

            int innerBlockClosingIndex = currentExpression.indexOf(CLOSING_BRACKET, innerBlockOpeningIndex + 1);
            if (innerBlockClosingIndex < 0) {
                throw new EnotInvalidArgumentException("missing closing bracket");
            }

            Pair<ExpressionFunction, Integer> functionIndex = getFunctionWithIndex(currentExpression, innerBlockOpeningIndex);
            boolean blockWithFunction = false;
            if (functionIndex != null) {
                innerBlockClosingIndex = functionIndex.getRight();
                blockWithFunction = true;
            }

            String expressionBlock = currentExpression.substring(innerBlockOpeningIndex + 1, innerBlockClosingIndex).trim();
            String blockName = String.format(BLOCK_NAME_TEMPLATE, (blocks.size() + 1));
            blocks.put(blockName, expressionBlock);

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

        return currentExpression;

    }

    private Pair<ExpressionFunction, Integer> getFunctionWithIndex(String currentExpression, int blockOpeningIndex) {

        int breakIndex = 0;
        for (int i = blockOpeningIndex - 1; i >= 0; i--) {
            char c = currentExpression.charAt(i);
            if (c == OPENING_BRACKET || c == '|' || c == '&' || c == '=' || c == '!' || c == '>' || c == '<') {
                breakIndex = i;
                break;
            }
        }

        if (blockOpeningIndex - breakIndex <= 0) {
            return null;
        }

        String functionCandidateName = currentExpression.substring(breakIndex, blockOpeningIndex).trim();

        return null;
    }

    private String removeSpaces(String input) {
        StringBuilder result = new StringBuilder();
        boolean insideLiteral = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'') {
                insideLiteral = !insideLiteral;
            }
            if (!insideLiteral && c != ' ' && c != '\n' && c != '\t' && c != '\r') {
                result.append(c);
            }
        }
        return result.toString();
    }

    public List<ComparisonExpression> parseBinaryExpression(String expression) {
        return List.of();
    }

    public ComparisonExpression parseComparisonExpression(String expression) {

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

        validateExpressionPart(left, "left", expression);
        validateExpressionPart(right, "right", expression);

        return ComparisonExpression.of(left.trim(), right.trim(), operator);

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

    private void validateExpressionPart(String expressionPart, String partName, String parentExpression) {

        if (StringUtils.isBlank(expressionPart)) {
            throw new EnotInvalidArgumentException("Blank " + partName + " part of expression: " + parentExpression);
        }

        String expressionPartTrimmed = expressionPart.trim();

        PlaceholderUtils.isPlaceholder(expressionPartTrimmed);
    }


    private void validateBrackets(String expression) {

        int openingBracketCounter = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == OPENING_BRACKET) {
                openingBracketCounter++;
            }
            if (c == CLOSING_BRACKET) {
                openingBracketCounter--;
            }
            if (openingBracketCounter < 0) {
                throw new EnotInvalidArgumentException("extra closing bracket");
            }
        }
        if (openingBracketCounter > 0) {
            throw new EnotInvalidArgumentException("missing closing bracket");
        }
    }
}
