package com.github.flexca.enot.core.system.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.value.ValueType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum SystemAttribute implements EnotAttribute {

    KIND("kind", ValueType.TEXT),
    ITEMS("items", ValueType.TEXT);

    private static final Map<String, SystemAttribute> BY_NAME = new HashMap<>();
    static {
        for(SystemAttribute value : values()) {
            BY_NAME.put(value.getName(), value);
        }
    }

    private final String name;

    @Getter
    private final ValueType valueType;

    @Override
    public SystemAttribute fromName(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

    @JsonCreator
    public static SystemAttribute fromJsonString(String name) {
        return StringUtils.isBlank(name) ? null : BY_NAME.get(name.toLowerCase());
    }
}
