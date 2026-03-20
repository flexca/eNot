package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.exception.EnotParseException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import com.github.flexca.enot.core.struct.type.EnotElementType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

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
        List<EnotElement> elements = new ArrayList<>(elementsArray.size());
        for (JsonNode itemNode : elementsArray) {
            if (itemNode.isObject()) {
                parseElement(itemNode.asObject());
            }
        }
        return elements;
    }

    private EnotElement parseElement(ObjectNode jsonElement) {

        EnotElement element = new EnotElement();

        JsonNode typeNode = jsonElement.get(ENOT_ELEMENT_TYPE_NAME);
        if (!typeNode.isString()) {
            throw new EnotParseException("Element type must be string");
        }
        EnotElementType type = enotRegistry.resolveElementType(typeNode.asString()).orElseThrow(() ->
            new EnotParseException("Element type must be not be empty"));
        element.setType(type);

        JsonNode attributesNode = jsonElement.get(ENOT_ELEMENT_ATTRIBUTES_NAME);
        if (!attributesNode.isObject()) {
            throw new EnotParseException("Element attributes must be JSON object");
        }
        Map<EnotAttribute, Object> attributes = new HashMap<>();
        for (String attributeName : attributesNode.asObject().propertyNames()) {
            JsonNode attributeValueNode = attributesNode.asObject().get(attributeName);
            attributes.put(attributeName, );
        }
        element.setAttributes(attributes);

        JsonNode bodyNode = jsonElement.get(ENOT_ELEMENT_BODY_NAME);
        if (bodyNode.isArray()) {
            element.setBody(parseElements(bodyNode.asArray()));
        } else if (bodyNode.isObject()) {
            element.setBody(parseElement(bodyNode.asObject()));
        } else if (bodyNode.isString()) {
            element.setBody(bodyNode.asString());
        } else if (bodyNode.isBigDecimal()) {
            element.setBody(bodyNode.asDecimal());
        } else if (bodyNode.isBigInteger()) {
            element.setBody(bodyNode.asBigInteger());
        } else if (bodyNode.isBoolean()) {
            element.setBody(bodyNode.asBoolean());
        } else if (bodyNode.isNull()) {
            // We allow empty body
        } else {
            throw new EnotParseException("Unexpected eNot element body type, expecting one of: object, array, text, number, boolean");
        }
        return element;
    }

}
