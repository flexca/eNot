package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.exception.EnotParseException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.type.EnotElementType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnotParser {

    public static final String ENOT_ELEMENT_TYPE_NAME = "type";
    public static final String ENOT_ELEMENT_ATTRIBUTES_NAME = "attributes";
    public static final String ENOT_ELEMENT_BODY_NAME = "body";

    private final EnotRegistry enotRegistry;
    private final ObjectMapper objectMapper;

    public EnotParser(EnotRegistry enotRegistry, ObjectMapper objectMapper) {
        this.enotRegistry = enotRegistry;
        this.objectMapper = objectMapper;
    }

    public List<EnotElement> parse(String json) {

        List<EnotElement> elements = new ArrayList<>();

        JsonNode rootNode = objectMapper.readValue(json, JsonNode.class);

        if (rootNode.isArray()) {
            elements.addAll(parseElements(rootNode.asArray()));
        } else if (rootNode.isObject()) {
            elements.add(parseElement(rootNode.asObject()));
        } else {
            throw new EnotParseException("eNot expecting object or array as root JSON node");
        }

        return elements;
    }

    private List<EnotElement> parseElements(ArrayNode elementsArray) {
        for (JsonNode item : elementsArray) {
            if (item.isObject()) {

            }
        }
    }

    private EnotElement parseElement(ObjectNode jsonElement) {

        EnotElement element = new EnotElement();

        JsonNode typeNode = jsonElement.get(ENOT_ELEMENT_TYPE_NAME);
        if (typeNode.isString()) {
            Optional<EnotElementType> type = enotRegistry.resolveElementType(typeNode.asString());
            if (type.isEmpty()) {
                throw new EnotParseException("Element type must be not be empty");
            }
            element.setType(type.get());
        }

        JsonNode attributesNode = jsonElement.get(ENOT_ELEMENT_ATTRIBUTES_NAME);


    }

}

