package io.github.flexca.enot.core.types.system.validation;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.expression.model.ExpressionBlock;
import io.github.flexca.enot.core.expression.model.ExpressionFunction;
import io.github.flexca.enot.core.expression.model.ExpressionLeaf;
import io.github.flexca.enot.core.expression.model.ExpressionNode;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.registry.EnotElementValidator;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.util.PlaceholderUtils;

import java.util.List;

public class SystemConditionValidator implements EnotElementValidator {

    @Override
    public void validateElement(EnotElement element, String parentPath, List<EnotJsonError> jsonErrors, EnotContext enotContext) {

        Object expression = element.getAttribute(SystemAttribute.EXPRESSION);
        String expressionPath = parentPath + "/" + SystemAttribute.EXPRESSION.getName();
        if (expression instanceof String stringExpression) {
            ExpressionBlock block;
            try {
                block = enotContext.getConditionExpressionParser().parse(stringExpression);
            } catch (Exception e) {
                jsonErrors.add(EnotJsonError.of(expressionPath, "failure during expression parsing, reason: "
                        + e.getMessage()));
                return;
            }
            checkPlaceholders(block, expressionPath, jsonErrors);
        } else {
            jsonErrors.add(EnotJsonError.of(expressionPath, "expression must be of string type"));
        }
    }

    private void checkPlaceholders(ExpressionBlock block, String currentPath, List<EnotJsonError> jsonErrors) {

        if (block instanceof ExpressionNode nodeBlock) {
            for (ExpressionBlock subBlock : nodeBlock.getParts()) {
                checkPlaceholders(subBlock, currentPath, jsonErrors);
            }
        } else if (block instanceof ExpressionFunction functionBlock) {
            for (ExpressionBlock subBlock : functionBlock.getArguments()) {
                checkPlaceholders(subBlock, currentPath, jsonErrors);
            }
        } else if (block instanceof ExpressionLeaf leafBlock) {
            if (CommonEnotValueType.PLACEHOLDER.equals(leafBlock.getValueType())) {
                if (leafBlock.getValue() instanceof String variableName) {
                    if (!PlaceholderUtils.isValidVariableName(variableName)) {
                        jsonErrors.add(EnotJsonError.of(currentPath, "invalid variable name: " + variableName + ", " +
                                "use only letters, digits or underscore"));
                    }
                }
            }
        } else {
            jsonErrors.add(EnotJsonError.of(currentPath, "unsupported ExpressionBlock type"));
        }
    }
}
