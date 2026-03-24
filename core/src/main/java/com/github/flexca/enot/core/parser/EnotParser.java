package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.exception.EnotInvalidAttributeException;
import com.github.flexca.enot.core.exception.EnotParseException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
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
    private final EnotParserValidator parserValidator;
    private final ObjectMapper objectMapper;

    public EnotParser(EnotRegistry enotRegistry, ObjectMapper objectMapper) {
        this.enotRegistry = enotRegistry;
        this.parserValidator = new EnotParserValidator(enotRegistry);
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
                elements.add(parseElement(itemNode.asObject()));
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
        EnotTypeSpecification typeSpecification = enotRegistry.getTypeSpecification(typeNode.asString()).orElseThrow(() ->
            new EnotParseException("Element type not found"));
        element.setType(typeSpecification.getTypeName());

        Map<EnotAttribute, Object> attributes = extractElementAttributes(jsonElement, typeSpecification);
        element.setAttributes(attributes);

        Optional<Object> elementBody = extractElementBody(jsonElement);
        if(elementBody.isPresent()) {
            element.setBody(elementBody.get());
        }

        parserValidator.validateElement(typeSpecification, element);
        typeSpecification.getElementValidator().validateElement(element);

        return element;
    }

    private Map<EnotAttribute, Object> extractElementAttributes(ObjectNode jsonElement, EnotTypeSpecification typeSpecification) {

        JsonNode attributesNode = jsonElement.get(ENOT_ELEMENT_ATTRIBUTES_NAME);
        if (!attributesNode.isObject()) {
            throw new EnotParseException("Element attributes must be JSON object");
        }
        Map<EnotAttribute, Object> attributes = new HashMap<>();
        for (String attributeName : attributesNode.asObject().propertyNames()) {
            EnotAttribute attribute = typeSpecification.resolveAttributeByName(attributeName);
            if(attribute == null) {
                throw new EnotInvalidAttributeException("Unsupported attribute " + attributeName + " for type "
                        + typeSpecification.getTypeName());
            }
            JsonNode attributeValueNode = attributesNode.asObject().get(attributeName);
            Optional<Object> attributeValue = extractPrimitiveValue(attributeValueNode);
            attributeValue.ifPresent(value -> attributes.put(attribute, value));
        }
        return attributes;
    }

    private Optional<Object> extractElementBody(ObjectNode jsonElement) {

        JsonNode bodyNode = jsonElement.get(ENOT_ELEMENT_BODY_NAME);
        if (bodyNode.isNull()) {
            return Optional.empty();
        } else if (bodyNode.isArray()) {
            return Optional.of(parseElements(bodyNode.asArray()));
        } else if (bodyNode.isObject()) {
            return Optional.of(parseElement(bodyNode.asObject()));
        }  else {
            Object bodyValue = extractPrimitiveValue(bodyNode).orElseThrow(() ->
                    new EnotParseException("Unexpected eNot element body type, expecting one of: object, array, text, number, boolean"));
            return Optional.of(bodyValue);
        }
    }

    private Optional<Object> extractPrimitiveValue(JsonNode valueNode) {

        if (valueNode.isString()) {
            return Optional.of(valueNode.asString());
        } else if (valueNode.isBigDecimal()) {
            return Optional.of(valueNode.asDecimal());
        } else if (valueNode.isBigInteger()) {
            return Optional.of(valueNode.asBigInteger());
        } else if (valueNode.isBoolean()) {
            return Optional.of(valueNode.asBoolean());
        }  else {
            return Optional.empty();
        }
    }
}
