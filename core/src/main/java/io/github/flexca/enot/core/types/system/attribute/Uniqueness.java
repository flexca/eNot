package io.github.flexca.enot.core.types.system.attribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Uniqueness {

    NONE("none"), ENFORCE("enforce");

    private static final Map<String, Uniqueness> BY_NAME = new HashMap<>();
    static {
        for(Uniqueness value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    Uniqueness(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Uniqueness fromName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }

    public static List<String> getNames() {
        return Arrays.stream(values()).map(Uniqueness::getName).toList();
    }
}
