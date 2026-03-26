package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.struct.EnotElement;

import java.util.List;
import java.util.Map;

public class EnotSerializer {

    private final EnotRegistry enotRegistry;

    public EnotSerializer(EnotRegistry enotRegistry) {
        this.enotRegistry = enotRegistry;
    }

    public byte[] serialize(String json, Map<String, Object> parameters) {
        return null;
    }

    public byte[] serialize(List<EnotElement> elements, Map<String, Object> parameters) {
        return null;
    }
}
