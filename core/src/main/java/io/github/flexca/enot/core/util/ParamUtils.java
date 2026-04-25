package io.github.flexca.enot.core.util;

import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.serializer.context.ContextArray;
import io.github.flexca.enot.core.serializer.context.ContextMap;
import io.github.flexca.enot.core.serializer.context.ContextNode;
import io.github.flexca.enot.core.serializer.context.ContextPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamUtils {

    private ParamUtils() {
    }

    public static Map<String, Object> toMap(ContextMap contextMap) {

        return nodeToMap(contextMap);
    }

    private static Object contextNodeToObject(ContextNode node) {

        if (node instanceof ContextMap mapNode) {
            return nodeToMap(mapNode);
        } else if (node instanceof ContextArray arrayNode) {
            return nodeToArray(arrayNode);
        } else if (node instanceof ContextPrimitive primitiveNode) {
            return primitiveNode.getValue();
        } else {
            throw new EnotInvalidArgumentException("unsupported ContextNode type");
        }
    }

    private static Map<String, Object> nodeToMap(ContextMap contextMap) {

        Map<String, Object> result = new HashMap<>();
        contextMap.getItems().forEach( (key, value) -> {
            result.put(key, contextNodeToObject(value));
        });
        return result;
    }

    private static List<Object> nodeToArray(ContextArray arrayNode) {

        List<Object> result = new ArrayList<>();
        for(ContextNode node : arrayNode.getItems()) {
            result.add(contextNodeToObject(node));
        }
        return result;
    }
}
