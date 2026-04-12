package com.github.flexca.enot.core.expression;

import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.expression.model.ConditionFunction;
import com.github.flexca.enot.core.expression.model.ExpressionBlock;
import com.github.flexca.enot.core.expression.model.ExpressionFunction;
import com.github.flexca.enot.core.expression.model.ExpressionLeaf;
import com.github.flexca.enot.core.expression.model.ExpressionNode;
import com.github.flexca.enot.core.expression.model.Operator;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConditionExpressionParser {

    private static final String BLOCK_NAME_PREFIX = "#block";
    private static final String BLOCK_NAME_TEMPLATE = "#block%d";
    private static final char OPENING_BRACKET = '(';
    private static final char CLOSING_BRACKET = ')';
    private static final char LITERAL = '\'';
    private static final char ARGUMENTS_SEPARATOR = ',';


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
            if (functionExtractionResult.getConditionFunction() != null) {
                block = parseFunctionExpression(subExpression, blocks, functionExtractionResult);
                innerBlockOpeningIndex = functionExtractionResult.getStartIndex();
            } else {
                block = parseBinaryExpression(subExpression, false, blocks);
                innerBlockOpeningIndex = functionExtractionResult.getStartIndex();
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

        return parseBinaryExpression(currentExpression, false, blocks);

    }

    private ExpressionBlock parseFunctionExpression(String expression, Map<String, ExpressionBlock> blocks,
                                                    FunctionExtractionResult functionExtractionResult) {

        boolean insideOfLiteral = false;
        int lastSeparatorPosition = -1;
        List<String> arguments = new ArrayList<>();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == LITERAL) {
                insideOfLiteral = !insideOfLiteral;
            }
            if (!insideOfLiteral) {
                if (c == ARGUMENTS_SEPARATOR) {
                    String argument = expression.substring(lastSeparatorPosition + 1, i);
                    arguments.add(argument);
                    lastSeparatorPosition = i;
                }
            }

            if (i == expression.length() - 1) {
                String argument = expression.substring(lastSeparatorPosition + 1);
                arguments.add(argument);
            }
        }

        if (functionExtractionResult.getConditionFunction().getArgumentsNumber() != arguments.size()) {
            throw new EnotInvalidArgumentException("function " + functionExtractionResult.getConditionFunction().getName() + " " +
                    "required " + functionExtractionResult.getConditionFunction().getArgumentsNumber() + " arguments, " +
                    "provided number of arguments: " + arguments.size());
        }

        List<ExpressionBlock> argumentExpressions = new ArrayList<>();
        for (String argument : arguments) {
            ExpressionBlock argumentExpression = parseBinaryExpression(argument, false, blocks);
            argumentExpressions.add(argumentExpression);
        }

        return new ExpressionFunction(functionExtractionResult.isInverted(), functionExtractionResult.getConditionFunction(), argumentExpressions);
    }

    private FunctionExtractionResult getFunctionWithIndex(String currentExpression, int blockOpeningIndex) {

        int breakIndex = 0;
        for (int i = blockOpeningIndex - 1; i >= 0; i--) {
            char c = currentExpression.charAt(i);
            if (c == OPENING_BRACKET || c == '|' || c == '&' || c == '=' /*|| c == '!'*/ || c == '>' || c == '<' || c == ARGUMENTS_SEPARATOR) {
                breakIndex = i + 1;
                break;
            }
        }

        if (blockOpeningIndex - breakIndex <= 0) {
            return new FunctionExtractionResult(null, breakIndex, false);
        }

        String functionCandidateName = currentExpression.substring(breakIndex, blockOpeningIndex).trim();
        boolean inverted = false;
        if (functionCandidateName.startsWith(Operator.NOT_OPERATOR.getOperator())) {
            if (blockOpeningIndex - breakIndex == 1) {
                return new FunctionExtractionResult(null, breakIndex + 1, true);
            }
            inverted = true;
            functionCandidateName = functionCandidateName.substring(1);
        }
        ConditionFunction conditionFunction = ConditionFunction.getByName(functionCandidateName);
        if (conditionFunction == null) {
            throw new EnotInvalidArgumentException("unsupported function: " + functionCandidateName);
        }

        return new FunctionExtractionResult(conditionFunction, breakIndex, inverted);
    }

    private ExpressionBlock parseBinaryExpression(String expression, boolean inverted, Map<String, ExpressionBlock> blocks) {

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
            throw new EnotInvalidArgumentException("missing operators in expression");
        }
        if (parts.size() - 1 < operators.size()) {
            throw new EnotInvalidArgumentException("extra operators in expression");
        }

        if (parts.size() == 1) {
            return parseComparisonExpression(parts.get(0), inverted, blocks);
        }

        List<ExpressionBlock> subExpressions = new ArrayList<>();
        for (String part : parts) {
            ExpressionBlock subExpression = parseComparisonExpression(part, false, blocks);
            subExpressions.add(subExpression);
        }

        return new ExpressionNode(inverted, subExpressions, operators.get(0));
    }

    private ExpressionBlock parseComparisonExpression(String expression, boolean inverted, Map<String, ExpressionBlock> blocks) {

        List<Operator> operators = new ArrayList<>();
        List<String> parts = new ArrayList<>();
        char previousChar = expression.charAt(0);
        boolean insideOfLiteral = previousChar == LITERAL;
        int lastPartPosition = -1;
        for (int i = 1; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);
            Operator currentOperator = null;

            if (!insideOfLiteral) {

                if (previousChar == '=' && currentChar == '=') {
                    currentOperator = Operator.EQUALS_OPERATOR;
                }
                if (previousChar == '!' && currentChar == '=') {
                    currentOperator = Operator.NOT_EQUALS_OPERATOR;
                }
                if (previousChar == '>' && currentChar == '=') {
                    currentOperator = Operator.GREATER_THAN_OR_EQUALS_OPERATOR;
                }
                if (previousChar == '>' && currentChar != '=') {
                    currentOperator = Operator.GREATER_THAN_OPERATOR;
                }
                if (previousChar == '<' && currentChar == '=') {
                    currentOperator = Operator.LESS_THAN_OR_EQUALS_OPERATOR;
                }
                if (previousChar == '<' && currentChar != '=') {
                    currentOperator = Operator.LESS_THAN_OPERATOR;
                }

                if (currentOperator != null) {
                    operators.add(currentOperator);
                    String part = expression.substring(lastPartPosition + 1, i - 1);
                    parts.add(part);
                    lastPartPosition = currentOperator.getOperator().length() == 1 ? i - 1 : i;
                }
            }

            if (currentChar == LITERAL) {
                insideOfLiteral = !insideOfLiteral;
            }

            if (i == expression.length() - 1) {
                String part = expression.substring(lastPartPosition + 1);
                parts.add(part);
            }

            previousChar = currentChar;
        }

        if (operators.size() > 1) {
            throw new EnotInvalidArgumentException("extra comparison operation " + operators.get(1).getOperator()
                    + " in expression: " + expression);
        }

        if (parts.size() > 2 || parts.isEmpty() || (parts.size() - 1 != operators.size())) {
            throw new EnotInvalidArgumentException("syntax error in expression: " + expression);
        }

        if (parts.size() == 1) {
            ExpressionBlock primitiveBlock = extractPrimitiveBlock(parts.get(0), expression, blocks);
            return inverted ? primitiveBlock.invert() : primitiveBlock;
        }

        List<ExpressionBlock> expressionParts = new ArrayList<>();
        for (String part : parts) {
            ExpressionBlock expressionPart = extractPrimitiveBlock(part, expression, blocks);
            expressionParts.add(expressionPart);
        }

        return new ExpressionNode(inverted, expressionParts, operators.get(0));
    }

    private ExpressionBlock extractPrimitiveBlock(String expressionPart, String parentExpression, Map<String, ExpressionBlock> blocks) {

        if (StringUtils.isBlank(expressionPart)) {
            throw new EnotInvalidArgumentException("blank expression in " + parentExpression);
        }
        String expressionPartTrimmed = expressionPart.trim();
        boolean inverted = false;
        if (expressionPartTrimmed.startsWith("!")) {
            expressionPartTrimmed = expressionPartTrimmed.substring(1);
            inverted = true;
        }

        if (PlaceholderUtils.isPlaceholder(expressionPartTrimmed)) {
            Optional<String> placeholderValue = PlaceholderUtils.extractPlaceholder(expressionPartTrimmed);
            if (placeholderValue.isPresent()) {
                return new ExpressionLeaf(inverted, CommonEnotValueType.PLACEHOLDER, placeholderValue.get());
            }
        }

        if (expressionPartTrimmed.startsWith(BLOCK_NAME_PREFIX)) {
            String blockIndexText = expressionPartTrimmed.substring(BLOCK_NAME_PREFIX.length());
            int blockIndex;
            try {
                blockIndex = Integer.parseInt(blockIndexText);
            } catch(Exception e) {
                throw new EnotInvalidArgumentException("undefined symbol " + blockIndexText);
            }
            ExpressionBlock existingBlock = blocks.get(String.format(BLOCK_NAME_TEMPLATE, blockIndex));
            if (existingBlock == null) {
                throw new EnotInvalidArgumentException("undefined symbol " + blockIndexText);
            }
            return inverted ? existingBlock.invert() : existingBlock;
        }

        if (expressionPartTrimmed.charAt(0) == LITERAL && expressionPartTrimmed.charAt(expressionPartTrimmed.length() - 1) == LITERAL) {
            return new ExpressionLeaf(inverted, CommonEnotValueType.TEXT, expressionPartTrimmed.substring(1, expressionPartTrimmed.length() - 1));
        }

        if ("null".equalsIgnoreCase(expressionPartTrimmed)) {
            return new ExpressionLeaf(inverted, CommonEnotValueType.NULL_VALUE, null);
        }

        if ("true".equalsIgnoreCase(expressionPartTrimmed) || "false".equalsIgnoreCase(expressionPartTrimmed)) {
            return new ExpressionLeaf(inverted, CommonEnotValueType.BOOLEAN, Boolean.valueOf(expressionPartTrimmed));
        }

        try {
            BigInteger integerCandidate = new BigInteger(expressionPartTrimmed, 10);
            return new ExpressionLeaf(inverted, CommonEnotValueType.INTEGER, integerCandidate);
        } catch (Exception e) {
            throw new EnotInvalidArgumentException("undefined symbol " + expressionPartTrimmed);
        }
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
            if (insideLiteral || (c != ' ' && c != '\n' && c != '\t' && c != '\r')) {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static class FunctionExtractionResult {

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

        char previousChar = expression.charAt(0);
        boolean insideOfLiteral = previousChar == LITERAL;
        StringBuilder result = new StringBuilder();
        int ignoreTill = -1;
        for (int i = 1; i < expression.length(); i++) {
            boolean addPrevious = true;
            char currentChar = expression.charAt(i);
            if(currentChar == LITERAL) {
                insideOfLiteral = !insideOfLiteral;
            }
            if(!insideOfLiteral) {
                if (previousChar == '!' && currentChar == '!' && i > ignoreTill + 1) {
                    ignoreTill = i;
                }
            }

            if (i - 1 > ignoreTill) {
                result.append(previousChar);
            }
            if ((i == expression.length() - 1) && i > ignoreTill) {
                result.append(currentChar);
            }

            previousChar = currentChar;
        }

        return result.toString();
    }
}
