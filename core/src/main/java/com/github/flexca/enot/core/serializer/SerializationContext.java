package com.github.flexca.enot.core.serializer;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class SerializationContext {



    public static class Builder {

        private final ObjectMapper objectMapper;

        public Builder(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public Builder withParams(Map<String, Object> params) {
            return this;
        }

        public Builder withParams(String json) {
            return this;
        }

        public Builder withParam(String key, Object value) {
            return this;
        }
    }
}
