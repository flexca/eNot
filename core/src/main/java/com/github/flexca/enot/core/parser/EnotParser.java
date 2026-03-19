package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.exception.EnotParseException;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.type.EnotElementType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnotParser {

    public static final String ENOT_ELEMENT_TYPE_NAME = "type";
    public static final String ENOT_ELEMENT_ATTRIBUTES_NAME = "attributes";
    public static final String ENOT_ELEMENT_BODY_NAME = "body";

    private final ObjectMapper objectMapper;

    public EnotParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<EnotElement> parse(String json) {

        List<EnotElement> elements = new ArrayList<>();

        JsonNode rootNode = objectMapper.readValue(json, JsonNode.class);

        if (rootNode.isArray()) {
            ArrayNode elementsList = rootNode.asArray();
        } else if (rootNode.isObject()) {
            ObjectNode element = rootNode.asObject();
        } else {
            throw new EnotParseException();
        }

        return elements;
    }

    private EnotElement parseElement(ObjectNode jsonElement) {

        JsonNode typeNode = jsonElement.get(ENOT_ELEMENT_TYPE_NAME);
        if (typeNode.isString()) {

        }

    }
}

