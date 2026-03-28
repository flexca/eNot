package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.struct.attribute.EnotAttribute;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

public class EnotParser {

    public static final String ENOT_ELEMENT_TYPE_NAME = "type";
    public static final String ENOT_ELEMENT_ATTRIBUTES_NAME = "attributes";
    public static final String ENOT_ELEMENT_BODY_NAME = "body";

    private static final String COMMON_ERROR_MESSAGE = "Error during parsing of eNot, reason: ";

    private final EnotRegistry enotRegistry;
    private final EnotParserValidator parserValidator;
    private final ObjectMapper objectMapper;

    public EnotParser(EnotRegistry enotRegistry, ObjectMapper objectMapper) {
        this.enotRegistry = enotRegistry;
        this.parserValidator = new EnotParserValidator(enotRegistry);
        this.objectMapper = objectMapper;
    }

    public List<EnotElement> parse(String json) throws EnotParsingException {

        String currentPath = "";
        if (StringUtils.isBlank(json)) {
            throw new EnotParsingException(COMMON_ERROR_MESSAGE,
                    Collections.singletonList(EnotJsonError.of(currentPath, "blank JSON input provided")));
        }

        List<EnotElement> elements = new ArrayList<>();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readValue(json, JsonNode.class);
        } catch(Exception e) {
            throw new EnotParsingException(COMMON_ERROR_MESSAGE,
                    Collections.singletonList(EnotJsonError.of(currentPath, e.getMessage())), e);
        }

        List<EnotJsonError> jsonErrors = new ArrayList<>();
        Throwable cause = null;

        if (rootNode.isArray()) {
            try {
                elements.addAll(parseElements(rootNode.asArray(), currentPath, jsonErrors));
            } catch (Exception e) {
                cause = e;
                jsonErrors.add(EnotJsonError.of(currentPath, e.getMessage()));
            }
            if (elements.isEmpty() && jsonErrors.isEmpty()) {
                jsonErrors.add(EnotJsonError.of(currentPath, "no elements found"));
            }
        } else if (rootNode.isObject()) {
            try {
                Optional<EnotElement> element = parseElement(rootNode.asObject(), currentPath, jsonErrors);
                element.ifPresent(elements::add);
            } catch (Exception e) {
                cause = e;
                jsonErrors.add(EnotJsonError.of(currentPath, e.getMessage()));
            }
        } else {
            jsonErrors.add(EnotJsonError.of(currentPath, "eNot expecting object or array as root JSON node"));
        }

        if (CollectionUtils.isNotEmpty(jsonErrors)) {
            if(cause == null) {
                throw new EnotParsingException(COMMON_ERROR_MESSAGE, jsonErrors);
            } else {
                throw new EnotParsingException(COMMON_ERROR_MESSAGE, jsonErrors, cause);
            }
        }

        return elements;
    }

    private List<EnotElement> parseElements(ArrayNode elementsArray, String parentPath, List<EnotJsonError> jsonErrors) {

        List<EnotElement> elements = new ArrayList<>(elementsArray.size());
        for (int i = 0; i < elementsArray.size(); i++) {
            JsonNode itemNode = elementsArray.get(i);
            String currentPath = parentPath + "/" + i;
            if (itemNode.isObject()) {
                Optional<EnotElement> element = parseElement(itemNode.asObject(), currentPath, jsonErrors);
                element.ifPresent(elements::add);
            } else {
                jsonErrors.add(EnotJsonError.of(currentPath, "eNot expecting object, but get " + itemNode.getNodeType().name()));
            }
        }
        return elements;
    }

    private Optional<EnotElement> parseElement(ObjectNode jsonElement, String parentPath, List<EnotJsonError> jsonErrors) {

        JsonNode typeNode = jsonElement.get(ENOT_ELEMENT_TYPE_NAME);
        if (typeNode == null) {
            jsonErrors.add(EnotJsonError.of(parentPath, "required eNot element field " + ENOT_ELEMENT_TYPE_NAME));
            return Optional.empty();
        }

        String typePath = parentPath + "/" + ENOT_ELEMENT_TYPE_NAME;
        if (!typeNode.isString()) {
            jsonErrors.add(EnotJsonError.of(typePath, "eNot element field value " + ENOT_ELEMENT_TYPE_NAME + " must be string, provided: "
                    + typeNode.getNodeType().name()));
            return Optional.empty();
        }

        String type = typeNode.asString();
        if (StringUtils.isBlank(type)) {
            jsonErrors.add(EnotJsonError.of(typePath, "eNot element field value " + ENOT_ELEMENT_TYPE_NAME + " is blank"));
            return Optional.empty();
        }

        Optional<EnotTypeSpecification> typeSpecificationCandidate = enotRegistry.getTypeSpecification(type);
        if (typeSpecificationCandidate.isEmpty()) {
            jsonErrors.add(EnotJsonError.of(typePath, "unsupported " + ENOT_ELEMENT_TYPE_NAME + " of eNot element: " + type
                    + ", make sure this type was added to EnotRegistry"));
            return Optional.empty();
        }

        EnotTypeSpecification typeSpecification = typeSpecificationCandidate.get();
        EnotElement element = new EnotElement();
        element.setType(typeSpecification.getTypeName());

        Map<EnotAttribute, Object> attributes = extractElementAttributes(jsonElement, typeSpecification, parentPath, jsonErrors);
        element.setAttributes(attributes);

        Optional<Object> elementBody = extractElementBody(jsonElement, parentPath, jsonErrors);
        elementBody.ifPresent(element::setBody);

        parserValidator.validateElement(typeSpecification, element, parentPath, jsonErrors);
        typeSpecification.getElementValidator().validateElement(element, parentPath, jsonErrors);

        return Optional.of(element);
    }

    private Map<EnotAttribute, Object> extractElementAttributes(ObjectNode jsonElement, EnotTypeSpecification typeSpecification, String parentPath, List<EnotJsonError> jsonErrors) {

        JsonNode attributesNode = jsonElement.get(ENOT_ELEMENT_ATTRIBUTES_NAME);
        if(attributesNode == null || attributesNode.isNull()) {
            return Collections.emptyMap();
        }

        String currentPath = parentPath + "/" + ENOT_ELEMENT_ATTRIBUTES_NAME;
        if (!attributesNode.isObject()) {
            jsonErrors.add(EnotJsonError.of(currentPath, "eNot element field " + ENOT_ELEMENT_ATTRIBUTES_NAME
                    + " must be JSON object, provided: " + attributesNode.getNodeType().name()));
            return Collections.emptyMap();
        }

        Map<EnotAttribute, Object> attributes = new HashMap<>();
        for (String attributeName : attributesNode.asObject().propertyNames()) {
            String attributePath = parentPath + "/" + attributeName;
            EnotAttribute attribute = typeSpecification.resolveAttributeByName(attributeName);
            if (attribute == null) {
                jsonErrors.add(EnotJsonError.of(attributePath, "unsupported attribute " + attributeName + " for element of type "
                        + typeSpecification.getTypeName()));
                continue;
            }
            JsonNode attributeValueNode = attributesNode.asObject().get(attributeName);
            if (attributeValueNode == null || attributeValueNode.isNull()) {
                jsonErrors.add(EnotJsonError.of(attributePath, " value for attribute " + attributeName + " must be set"));
                continue;
            }
            Optional<Object> attributeValue = extractAttributeValue(attribute, attributeValueNode, currentPath, jsonErrors);
            if (attributeValue.isPresent()) {
                attributes.put(attribute, attributeValue.get());
            }
        }
        return attributes;
    }

    private Optional<Object> extractElementBody(ObjectNode jsonElement, String parentPath, List<EnotJsonError> jsonErrors) {

        String currentPath = parentPath + "/" + ENOT_ELEMENT_BODY_NAME;

        JsonNode bodyNode = jsonElement.get(ENOT_ELEMENT_BODY_NAME);
        if (bodyNode == null || bodyNode.isNull()) {
            return Optional.empty();
        } else if (bodyNode.isArray()) {
            ArrayNode bodyArrayNode = bodyNode.asArray();
            if(bodyArrayNode.isEmpty()) {
                return Optional.empty();
            }
            if (bodyArrayNode.get(0).isObject()) {
                List<EnotElement> elements = parseElements(bodyArrayNode, currentPath, jsonErrors);
                return CollectionUtils.isEmpty(elements) ? Optional.empty() : Optional.of(elements);
            } else {
                List<Object> primitiveValues = new ArrayList<>();
                int i = 0;
                for(JsonNode primitiveItem : bodyArrayNode) {
                    Optional<Object> objectBody = extractPrimitiveValue(primitiveItem);
                    if(objectBody.isEmpty()) {
                        jsonErrors.add(EnotJsonError.of(currentPath + "/" + i, "unexpected eNot element body JSON type: " + bodyNode.getNodeType().name()
                                + ", expecting boolean, number, string, object or array"));
                    } else {
                        primitiveValues.add(objectBody.get());
                    }
                    i++;
                }
                return CollectionUtils.isEmpty(primitiveValues) ? Optional.empty() : Optional.of(primitiveValues);
            }
        } else if (bodyNode.isObject()) {
            Optional<EnotElement> element = parseElement(bodyNode.asObject(), currentPath, jsonErrors);
            return element.isEmpty() ? Optional.empty() : Optional.of(element.get());
        }  else {
            Optional<Object> objectBody = extractPrimitiveValue(bodyNode);
            if (objectBody.isEmpty()) {
                jsonErrors.add(EnotJsonError.of(currentPath, "unexpected eNot element body JSON type: " + bodyNode.getNodeType().name()
                        + ", expecting boolean, number, string, object or array"));
            }
            return objectBody;
        }
    }

    private Optional<Object> extractAttributeValue(EnotAttribute attribute, JsonNode valueNode, String parentPath, List<EnotJsonError> jsonErrors) {

        String currentPath = parentPath + "/" + attribute.getName();
        if (valueNode.isArray()) {
            if (!attribute.getValueSpecification().isAllowMultipleValues()) {
                jsonErrors.add(EnotJsonError.of(currentPath, "multiple values is not allowed for attribute " + attribute.getName()));
                return Optional.empty();
            }
            List<Object> attributeValues = new ArrayList<>();
            for (int i = 0; i < valueNode.asArray().size(); i++) {
                JsonNode itemNode = valueNode.asArray().get(i);
                Optional<Object> primitiveValue = extractPrimitiveValue(itemNode);
                if (primitiveValue.isEmpty()) {
                    jsonErrors.add(EnotJsonError.of(currentPath, "unexpected eNot attribute + " + attribute.getName() + " +  value JSON type: "
                            + itemNode.getNodeType().name() + ", expecting boolean, number or string"));
                } else {
                    attributeValues.add(primitiveValue.get());
                }
            }
            return attributeValues.isEmpty() ? Optional.empty() : Optional.of(attributeValues);
        } else {
            Optional<Object> primitiveValue = extractPrimitiveValue(valueNode);
            if (primitiveValue.isEmpty()) {
                jsonErrors.add(EnotJsonError.of(currentPath, "unexpected eNot attribute + " + attribute.getName() + " +  value JSON type: "
                        + valueNode.getNodeType().name() + ", expecting boolean, number or string"));
            }
            return primitiveValue;
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
