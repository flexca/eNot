package io.github.flexca.enot.core.types.system.attribute;

import java.util.HashMap;
import java.util.Map;

public enum ByteOrder {

    BIG_ENDIAN("big_endian"), LITTLE_ENDIAN("little_endian");

    private static final Map<String, ByteOrder> BY_NAME = new HashMap<>();
    static {
        for(ByteOrder value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    private ByteOrder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ByteOrder fromName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
