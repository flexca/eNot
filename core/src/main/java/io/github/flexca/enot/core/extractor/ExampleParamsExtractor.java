package io.github.flexca.enot.core.extractor;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.element.value.CommonEnotValueType;
import io.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import io.github.flexca.enot.core.expression.model.ExpressionBlock;
import io.github.flexca.enot.core.expression.model.ExpressionFunction;
import io.github.flexca.enot.core.expression.model.ExpressionLeaf;
import io.github.flexca.enot.core.expression.model.ExpressionNode;
import io.github.flexca.enot.core.registry.EnotElementPathAltering;
import io.github.flexca.enot.core.registry.EnotElementPathAlteringType;
import io.github.flexca.enot.core.registry.EnotElementSpecification;
import io.github.flexca.enot.core.registry.EnotTypeSpecification;
import io.github.flexca.enot.core.serializer.context.ContextArray;
import io.github.flexca.enot.core.serializer.context.ContextMap;
import io.github.flexca.enot.core.serializer.context.ContextNode;
import io.github.flexca.enot.core.serializer.context.ContextPrimitive;
import io.github.flexca.enot.core.types.system.SystemKind;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.util.PlaceholderUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExampleParamsExtractor {

    private static final String GENERIC_EXAMPLE_VALUE = "replace with your value";

    private final EnotContext enotContext;

    public ExampleParamsExtractor(EnotContext enotContext) {
        this.enotContext = enotContext;
    }

    public ContextMap extractExampleParams(List<EnotElement> elements) {

        Map<String, ContextNode> items = new HashMap<>();
        Map<String, ContextNode> globalItems = new HashMap<>();
        for(EnotElement element : elements) {
            extractPlaceholdersFromElement(element, items, globalItems);
        }
        items.putAll(globalItems);
        ContextMap contextMap = new ContextMap();
        contextMap.setItems(items);
        return contextMap;
    }

    private void extractPlaceholdersFromElement(EnotElement element, Map<String, ContextNode> items, Map<String, ContextNode> globalItems) {

        EnotTypeSpecification typeSpecification = enotContext.getEnotRegistry().getTypeSpecification(element.getType())
                .orElseThrow(() -> new EnotInvalidConfigurationException("no specification found for type: " + element.getType()));
        EnotElementSpecification elementSpecification = typeSpecification.getElementSpecification(element);

        extractPlaceholdersFromAttributes(element, items, globalItems);

        EnotElementPathAltering pathAltering = typeSpecification.getPathAltering(element);
        if(EnotElementPathAlteringType.ARRAY_SCOPE.equals(pathAltering.getType())) {
            Map<String, ContextNode> subItems = new HashMap<>();
            ContextMap subMap = new ContextMap();
            subMap.setItems(subItems);
            List<ContextNode> subArrayItems = new ArrayList<>();
            ContextArray subArray = new ContextArray();
            subArrayItems.add(subMap);
            subArray.setItems(subArrayItems);
            items.put(pathAltering.getKey(), subArray);
            extractPlaceholdersFromElementBody(element.getBody(), elementSpecification, subItems, globalItems);
        } else if(EnotElementPathAlteringType.MAP_SCOPE.equals(pathAltering.getType())) {
            Map<String, ContextNode> subItems = new HashMap<>();
            ContextMap subMap = new ContextMap();
            subMap.setItems(subItems);
            items.put(pathAltering.getKey(), subMap);
            extractPlaceholdersFromElementBody(element.getBody(), elementSpecification, subItems, globalItems);
        } else {
            extractPlaceholdersFromElementBody(element.getBody(), elementSpecification, items, globalItems);
        }
    }

    private void extractPlaceholdersFromElementBody(Object objectBody, EnotElementSpecification elementSpecification,
                                                    Map<String, ContextNode> items, Map<String, ContextNode> globalItems ) {

        if (objectBody instanceof Collection<?> collectionBody) {
            for (Object childObject : collectionBody) {
                extractPlaceholdersFromElementBody(childObject, elementSpecification, items, globalItems);
            }
        } else if (objectBody instanceof EnotElement elementBody) {
            extractPlaceholdersFromElement(elementBody, items, globalItems);
        } else {
            if (PlaceholderUtils.isPlaceholder(objectBody)) {
                ContextPrimitive contextPrimitive = new ContextPrimitive();
                contextPrimitive.setValue(GENERIC_EXAMPLE_VALUE);
                Optional<String> placeholder = PlaceholderUtils.extractPlaceholder(objectBody);
                placeholder.ifPresent(variableName -> {
                    if (PlaceholderUtils.isGlobalVariable(variableName)) {
                        globalItems.put(variableName, contextPrimitive);
                    } else {
                        items.put(variableName, contextPrimitive);
                    }
                });
            }
        }
    }

    /**
     * Extracts placeholders from element attributes that allow parameterisation at runtime.
     * <p>
     * Currently the only such attribute is {@code expression} on {@code condition} elements — it is a parameterised
     * formula evaluated during serialisation, so its placeholders must appear in the example params just like body
     * placeholders do.
     * <p>
     * This method intentionally references {@link SystemTypeSpecification} and {@link SystemKind} directly, which
     * breaks the Open/Closed Principle (a new type with expression-like attributes would require a new branch here)
     * and the Dependency Inversion Principle (high-level extractor depends on low-level type details). The acceptable
     * trade-off is that {@code expression} is the only attribute of this kind and no other type is expected to grow
     * one. If that assumption changes, the right fix is a default method on {@link EnotTypeSpecification} such as
     * {@code getAttributesWithPlaceholders(EnotElement)} so each type owns its own declaration.
     */
    private void extractPlaceholdersFromAttributes(EnotElement element, Map<String, ContextNode> items, Map<String, ContextNode> globalItems) {

        if (SystemTypeSpecification.TYPE_NAME.equalsIgnoreCase(element.getType()) && SystemKind.CONDITION.equals(element.getAttribute(SystemAttribute.KIND))) {
            Object expressionObject = element.getAttribute(SystemAttribute.EXPRESSION);
            if (expressionObject instanceof String expression) {
                ExpressionBlock rootBlock = enotContext.getConditionExpressionParser().parse(expression);
                extractPlaceholdersFromExpressionBlock(rootBlock, items, globalItems);
            }
        }
    }

    private void extractPlaceholdersFromExpressionBlock(ExpressionBlock block, Map<String, ContextNode> items, Map<String, ContextNode> globalItems) {

        if (block instanceof ExpressionNode nodeBlock) {
            if (nodeBlock.getParts() != null) {
                for (ExpressionBlock subBlock : nodeBlock.getParts()) {
                    extractPlaceholdersFromExpressionBlock(subBlock, items, globalItems);
                }
            }
        } else if (block instanceof ExpressionFunction functionBlock) {
            if (functionBlock.getArguments() != null) {
                for (ExpressionBlock subBlock : functionBlock.getArguments()) {
                    extractPlaceholdersFromExpressionBlock(subBlock, items, globalItems);
                }
            }
        } else if (block instanceof ExpressionLeaf leafBlock) {
            if (CommonEnotValueType.PLACEHOLDER.equals(leafBlock.getValueType())) {
                ContextPrimitive contextPrimitive = new ContextPrimitive();
                contextPrimitive.setValue(GENERIC_EXAMPLE_VALUE);
                if (leafBlock.getValue() instanceof String stringValue) {
                    if (PlaceholderUtils.isGlobalVariable(stringValue)) {
                        globalItems.put(stringValue, contextPrimitive);
                    } else {
                        items.put(stringValue, contextPrimitive);
                    }
                }
            }
        }
    }
}
