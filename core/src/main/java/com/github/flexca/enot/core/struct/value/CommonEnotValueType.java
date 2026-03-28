package com.github.flexca.enot.core.struct.value;

import java.util.Collections;
import java.util.Set;

public enum CommonEnotValueType implements EnotValueType {

    BOOLEAN("boolean", Collections.emptySet(), true),
    BINARY("binary", Collections.emptySet(), false),
    INTEGER("integer", Collections.emptySet(), true),
    TEXT("text", Collections.emptySet(), true),
    PLACEHOLDER("placeholder", Collections.emptySet(), false),
    OBJECT_IDENTIFIER("object_identifier", Collections.emptySet(), false),
    DATE_TIME("date_time", Collections.emptySet(), false),
    ELEMENT("element", Collections.emptySet(), false),
    EMPTY("empty", Collections.emptySet(), false);

    private final String name;
    private final Set<EnotValueType> superTypes;
    private final boolean allowedForAttributes;

    private CommonEnotValueType(String name, Set<EnotValueType> superTypes, boolean allowedForAttributes) {
        this.name = name;
        this.superTypes = Collections.unmodifiableSet(superTypes);
        this.allowedForAttributes = allowedForAttributes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<EnotValueType> getSuperTypes() {
        return superTypes;
    }

    @Override
    public boolean isAllowedForAttributes() {
        return allowedForAttributes;
    }
}
