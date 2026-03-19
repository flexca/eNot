package com.github.flexca.enot.core;

import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.struct.EnotElement;
import lombok.RequiredArgsConstructor;

import java.util.List;

public class Enot {

    private final EnotRegistry enotRegistry;
    private final EnotParser enotParser;
    private final EnotSerializer enotSerializer;

    public Enot(EnotRegistry enotRegistry) {
        this.enotRegistry = enotRegistry;
        enotParser = new EnotParser();
        enotSerializer = new EnotSerializer();
    }

    public List<EnotElement> parse(String json) {
        return enotParser.parse(json);
    }

    public byte[] serialize() {
        return null;
    }

}
