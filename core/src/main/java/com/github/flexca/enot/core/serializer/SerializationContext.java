package com.github.flexca.enot.core.serializer;

import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SerializationContext {

    private final Map<String, Object> params;
    private final Map<String, Object> globalParams;

    private SerializationContext(Map<String, Object> params, Map<String, Object> globalParams) {
        this.params = Collections.unmodifiableMap(params);
        this.globalParams = Collections.unmodifiableMap(globalParams);
    }

    public static class Builder {

        private Map<String, Object> params = new HashMap<>();
        private Map<String, Object> globalParams = new HashMap<>();

        private final ObjectMapper objectMapper;

        public Builder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public Builder withParams(Map<String, Object> params) {
            params.putAll(params);
            return this;
        }

        public Builder withParams(String json) {

            return this;
        }

        public Builder withParam(String key, Object value) {
            params.put(key, value);
            return this;
        }

        public Builder withGlobalParams(Map<String, Object> params) {
            globalParams.putAll(params);
            return this;
        }

        public Builder withGlobalParam(String key, Object value) {
            globalParams.put(key, value);
            return this;
        }

        public SerializationContext build() {
            return new SerializationContext(params, globalParams);
        }
    }
}
