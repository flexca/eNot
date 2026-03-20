package com.github.flexca.enot.core.registry;

import com.github.flexca.enot.core.struct.type.EnotElementType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnotRegistry {


    private final Map<String, EnotElementType> elementTypes = new HashMap<>();

    public Optional<EnotElementType> resolveElementType(String typeString) {
        EnotElementType type = elementTypes.get(typeString);
        return type == null ? Optional.empty() : Optional.of(type);
    }
}
