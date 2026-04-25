package io.github.flexca.enot.core.parser;

import java.util.HashMap;
import java.util.Map;

public enum EnotInputFormat {

    JSON("json"), YAML("yaml"), UNSUPPORTED("unsupported");

    private static final Map<String, EnotInputFormat> BY_NAME = new HashMap<>();
    static {
        for(EnotInputFormat value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    EnotInputFormat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EnotInputFormat fromName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
