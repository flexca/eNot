package io.github.flexca.enot.core.types.system.attribute;

import java.util.HashMap;
import java.util.Map;

public enum BitOrder {

    MSB_FIRST("msb_first"), LSB_FIRST("lsb_first");

    private static final Map<String, BitOrder> BY_NAME = new HashMap<>();
    static {
        for(BitOrder value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    private BitOrder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static BitOrder fromName(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase());
    }
}
