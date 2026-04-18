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

/**
 * Holds the input parameters for a single serialization run and tracks the
 * current position within a nested parameter tree.
 *
 * <p>Parameters are stored as a typed tree of {@link ContextNode} objects
 * ({@link ContextMap}, {@link ContextArray}, {@link ContextPrimitive}) that
 * mirrors the shape of the original JSON / {@code Map} input. This allows the
 * serializer to navigate into nested objects and arrays while keeping full
 * access to every resolved value via placeholder lookup.</p>
 *
 * <h2>Params vs global params</h2>
 * <ul>
 *   <li><b>params</b> – per-serialization values, scoped to the current path
 *       position. Placeholders of the form {@code ${name}} are resolved
 *       against the node that the current path points to.</li>
 *   <li><b>globalParams</b> – cross-cutting values that are always resolved
 *       from the root regardless of the current path. Placeholders must use
 *       the {@code global.} prefix, e.g. {@code ${global.env}}.</li>
 * </ul>
 *
 * <h2>Path navigation</h2>
 * The serializer drives path navigation through
 * {@link #pathStepForward(String)} and {@link #pathStepBack()} while
 * iterating over LOOP elements, and through {@link #nextIndex()} /
 * {@link #hasNext()} to advance the array cursor.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * SerializationContext ctx = new SerializationContext.Builder(objectMapper)
 *         .withParams(Map.of("cn", "Alice", "san", List.of("alice@example.com")))
 *         .withGlobalParam("env", "prod")
 *         .build();
 * }</pre>
 *
 * <p>Instances are <em>not</em> thread-safe — each serialization thread must
 * use its own {@code SerializationContext}.</p>
 */
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

    /**
     * Resolves a placeholder name to its current value.
     *
     * <p>If {@code name} starts with {@value com.github.flexca.enot.core.util.PlaceholderUtils#GLOBAL_PARAM_PREFIX}
     * the value is looked up in the global params map (prefix is stripped first).
     * Otherwise the lookup is performed against the node that the current path
     * points to:</p>
     * <ul>
     *   <li>If the current node is a {@link ContextMap}, the named child is
     *       returned directly.</li>
     *   <li>If the current node is a {@link ContextArray}, the element at the
     *       current iteration index is inspected; if that element is itself a
     *       map the named child is returned.</li>
     * </ul>
     *
     * @param name placeholder name as extracted by
     *             {@link com.github.flexca.enot.core.util.PlaceholderUtils#extractPlaceholder}
     *             (i.e. without the {@code ${…}} wrapper)
     * @return the resolved value, or {@code null} if the name is not present
     */
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

    /**
     * Pushes a new path segment onto the navigation stack, descending into a
     * child node with the given key.
     *
     * <p>Must be balanced with a corresponding call to {@link #pathStepBack()}.
     * Typically called by the LOOP serializer before iterating over a nested
     * array field.</p>
     *
     * @param pathStep the key of the child node to descend into
     */
    public void pathStepForward(String pathStep) {
        currentPath.add(new ContextState(pathStep, 0));
    }

    /**
     * Pops the most recent path segment from the navigation stack, returning
     * to the parent node. A call on the root (stack size 1) is silently
     * ignored.
     */
    public void pathStepBack() {
        if (currentPath.size() == 1) {
            return;
        }
        currentPath.remove(currentPath.size() - 1);
    }

    /**
     * Returns {@code true} if the current array cursor has not yet reached
     * the end of the array at the current path position.
     *
     * <p>For non-array nodes this always returns {@code true} on the first
     * call and {@code false} after {@link #nextIndex()} has been called once,
     * effectively treating a scalar as a single-element sequence.</p>
     *
     * @return {@code true} while there are more elements to process
     */
    public boolean hasNext() {
        ContextNode currentContext = getCurrentContext();
        ContextState contextState = currentPath.get(currentPath.size() - 1);
        int size = 1;
        if (currentContext instanceof ContextArray contextArray) {
            size = (contextArray.getItems() == null) ? 0 : contextArray.getItems().size();
        }
        return contextState.getIndex() < size;
    }

    /**
     * Advances the array cursor at the current path position by one.
     *
     * @throws EnotInvalidArgumentException if the cursor is already past the
     *         last element
     */
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

    /**
     * Resets the array cursor at the current path position back to zero,
     * allowing the current array to be iterated again from the beginning.
     */
    public void resetIndex() {
        ContextState contextState = currentPath.get(currentPath.size() - 1);
        contextState.setIndex(0);
    }

    public List<ContextState> getCurrentPath() {
        return currentPath;
    }

    /**
     * Builder for {@link SerializationContext}.
     *
     * <p>A Jackson {@link ObjectMapper} is required to deserialize params
     * supplied as a JSON string. The same instance should be reused across
     * multiple builds to avoid repeated initialisation overhead.</p>
     */
    public static class Builder {

        private final ObjectMapper objectMapper;

        private Map<String, Object> params = new HashMap<>();
        private Map<String, Object> globalParams = new HashMap<>();

        /**
         * Creates a new builder with the given {@link ObjectMapper}.
         *
         * @param objectMapper mapper used to deserialize JSON params; must not
         *                     be {@code null}
         */
        public Builder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        /**
         * Merges the given map into the params for this context.
         *
         * @param params key/value pairs to add; nested maps and collections
         *               are supported
         * @return this builder
         */
        public Builder withParams(Map<String, Object> params) {
            this.params.putAll(params);
            return this;
        }

        /**
         * Deserializes {@code json} and merges the resulting map into params.
         *
         * @param json a JSON object string, e.g. {@code {"cn":"Alice"}}
         * @return this builder
         */
        public Builder withParams(String json) {
            Map<String, Object> params = jsonToMap(objectMapper, json);
            this.params.putAll(params);
            return this;
        }

        /**
         * Adds a single param entry.
         *
         * @param key   param name
         * @param value param value
         * @return this builder
         */
        public Builder withParam(String key, Object value) {
            this.params.put(key, value);
            return this;
        }

        /**
         * Merges the given map into the global params.
         *
         * @param params global key/value pairs; resolved via the
         *               {@code global.} placeholder prefix
         * @return this builder
         */
        public Builder withGlobalParams(Map<String, Object> params) {
            this.globalParams.putAll(params);
            return this;
        }

        /**
         * Adds a single global param entry.
         *
         * @param key   global param name (without the {@code global.} prefix)
         * @param value global param value
         * @return this builder
         */
        public Builder withGlobalParam(String key, Object value) {
            this.globalParams.put(key, value);
            return this;
        }

        /**
         * Builds and returns the {@link SerializationContext}.
         *
         * @return a new, immutable-params context ready for serialization
         */
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

        return extractMap(input);
    }

    private static ContextNode extractNode(Object input) {

        if (input instanceof Map<?, ?> mapChild) {
            return extractMap(mapChild);
        } else if (input instanceof Collection<?> arrayChild) {
            return extractArray(arrayChild);
        } else {
            return extractPrimitive(input);
        }
    }

    private static ContextMap extractMap(Map<?, ?> input) {

        ContextMap contextMap = new ContextMap();
        Map<String, ContextNode> items = new HashMap<>();
        contextMap.setItems(items);
        input.forEach((key, value) -> {
            if (key instanceof String stringKey) {
                items.put(stringKey, extractNode(value));
            } else {
                throw new EnotInvalidArgumentException("key of params map node must be string");
            }
        });
        return contextMap;
    }

    private static ContextArray extractArray(Collection<?> input) {

        ContextArray contextArray = new ContextArray();
        List<ContextNode> items = new ArrayList<>();
        contextArray.setItems(items);
        for (Object value : input) {
            items.add(extractNode(value));
        }
        return contextArray;
    }

    private static ContextPrimitive extractPrimitive(Object input) {

        ContextPrimitive contextPrimitive = new ContextPrimitive();
        contextPrimitive.setValue(input);
        return contextPrimitive;
    }

    private static Map<String, Object> jsonToMap(ObjectMapper objectMapper, String json) {

        TypeReference<Map<String, Object>> paramsType = new TypeReference<>() {};
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
