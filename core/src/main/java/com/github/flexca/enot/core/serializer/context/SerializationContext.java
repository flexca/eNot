package com.github.flexca.enot.core.serializer.context;

import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializationContext {

    private final ObjectMapper objectMapper;

    private final ContextNode params;

    private final Map<String, Object> globalParams;

    private final List<ContextState> currentPath = new ArrayList<>();

    private SerializationContext(ObjectMapper objectMapper, Map<String, Object> params, Map<String, Object> globalParams) {
        this.objectMapper = objectMapper;
        this.params = fromMap(params);
        this.globalParams = Collections.unmodifiableMap(globalParams);
        this.currentPath.add(new ContextState(StringUtils.EMPTY, 0));
    }

    public Object resolvePlaceholderValue(String name) {
        if (name.startsWith(PlaceholderUtils.GLOBAL_PARAM_PREFIX)) {
            return globalParams.get(name.substring(PlaceholderUtils.GLOBAL_PARAM_PREFIX.length()));
        } else {
            ContextNode currentContext = getCurrentContext();
            if (currentContext instanceof ContextMap contextMap) {
                ContextNode node = contextMap.get(name);
                return node == null ? null : node.getValue();
            } else if (currentContext instanceof ContextArray contextArray) {
                ContextState contextState = currentPath.get(currentPath.size() - 1);
                int size = contextArray.getItems() == null ? 0 : contextArray.getItems().size();
                if (contextState.getIndex() >= size) {
                    return null;
                }
                ContextNode iterationNode = contextArray.getItems().get(contextState.getIndex());
                if (iterationNode instanceof ContextMap contextMap) {
                    ContextNode node = contextMap.get(name);
                    return node == null ? null : node.getValue();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public void pathStepForward(String pathStep) {
        currentPath.add(new ContextState(pathStep, 0));
    }

    public void pathStepBack() {
        if (currentPath.size() == 1) {
            return;
        }
        currentPath.remove(currentPath.size() - 1);
    }

    public boolean hasNext() {
        ContextNode currentContext = getCurrentContext();
        ContextState contextState = currentPath.get(currentPath.size() - 1);
        int size = 1;
        if (currentContext instanceof ContextArray contextArray) {
            size = (contextArray.getItems() == null) ? 0 : contextArray.getItems().size();
        }
        return contextState.getIndex() < size;
    }

    public void nextIndex() {
        ContextNode currentContext = getCurrentContext();
        ContextState contextState = currentPath.get(currentPath.size() - 1);
        if (currentContext instanceof ContextArray contextArray) {
            int size = (contextArray.getItems() == null) ? 0 : contextArray.getItems().size();
            int currentIndex = contextState.getIndex();
            if (currentIndex < size) {
                contextState.setIndex(currentIndex + 1);
            } else {
                throw new EnotInvalidArgumentException("array out of bound");
            }
        } else {
            int size = 1;
            int currentIndex = contextState.getIndex();
            if (currentIndex < size) {
                contextState.setIndex(currentIndex + 1);
            } else {
                throw new EnotInvalidArgumentException("array out of bound");
            }
        }
    }

    public void resetIndex() {
        ContextState contextState = currentPath.get(currentPath.size() - 1);
        contextState.setIndex(0);
    }

    public List<ContextState> getCurrentPath() {
        return currentPath;
    }

    public static class Builder {

        private final ObjectMapper objectMapper;

        private Map<String, Object> params = new HashMap<>();
        private Map<String, Object> globalParams = new HashMap<>();

        public Builder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public Builder withParams(Map<String, Object> params) {
            this.params.putAll(params);
            return this;
        }

        public Builder withParams(String json) {
            Map<String, Object> params = jsonToMap(objectMapper, json);
            this.params.putAll(params);
            return this;
        }

        public Builder withParam(String key, Object value) {
            this.params.put(key, value);
            return this;
        }

        public Builder withGlobalParams(Map<String, Object> params) {
            this.globalParams.putAll(params);
            return this;
        }

        public Builder withGlobalParam(String key, Object value) {
            this.globalParams.put(key, value);
            return this;
        }

        public SerializationContext build() {
            return new SerializationContext(objectMapper, params, globalParams);
        }
    }

    private static class ContextState {

        private String path;
        private int index;

        public ContextState(String path, int index) {
            this.path = path;
            this.index = index;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }


    private static ContextNode fromMap(Map<String, Object> input) {

        return extractMap(null, input);
    }

    private static ContextNode extractNode(ContextNode parent, Object input) {

        if (input instanceof Map<?, ?> mapChild) {
            return extractMap(parent, mapChild);
        } else if (input instanceof Collection<?> arrayChild) {
            return extractArray(parent, arrayChild);
        } else {
            return extractPrimitive(parent, input);
        }
    }

    private static ContextMap extractMap(ContextNode parent, Map<?, ?> input) {

        ContextMap contextMap = new ContextMap();
        contextMap.setParent(parent);
        Map<String, ContextNode> items = new HashMap<>();
        contextMap.setItems(items);
        input.forEach((key, value) -> {
            if (key instanceof String stringKey) {
                items.put(stringKey, extractNode(contextMap, value));
            } else {
                throw new EnotInvalidArgumentException("key of params map node must be string");
            }
        });
        return contextMap;
    }

    private static ContextArray extractArray(ContextNode parent, Collection<?> input) {

        ContextArray contextArray = new ContextArray();
        contextArray.setParent(parent);
        List<ContextNode> items = new ArrayList<>();
        contextArray.setItems(items);
        for (Object value : input) {
            items.add(extractNode(contextArray, value));
        }
        return contextArray;
    }

    private static ContextPrimitive extractPrimitive(ContextNode parent, Object input) {

        ContextPrimitive contextPrimitive = new ContextPrimitive();
        contextPrimitive.setParent(parent);
        contextPrimitive.setValue(input);
        return contextPrimitive;
    }

    private static Map<String, Object> jsonToMap(ObjectMapper objectMapper, String json) {

        TypeReference<Map<String, Object>> paramsType = new TypeReference<>() {
        };
        Map<String, Object> params = objectMapper.readValue(json, paramsType);
        return params;
    }

    private ContextNode getCurrentContext() {

        if (currentPath.size() == 1) {
            return params;
        }
        ContextNode currentContext = params;
        for (int i = 1; i < currentPath.size(); i++) {
            ContextState state = currentPath.get(i);
            if (currentContext instanceof ContextMap contextMap) {
                currentContext = contextMap.get(state.getPath());
            } else if (currentContext instanceof ContextArray contextArray) {
                int outerIndex = currentPath.get(i - 1).getIndex();
                ContextNode item = contextArray.getItems().get(outerIndex);
                if (item instanceof ContextMap itemMap) {
                    currentContext = itemMap.get(state.getPath());
                } else {
                    currentContext = item;
                }
            }
        }
        return currentContext;
    }
}
