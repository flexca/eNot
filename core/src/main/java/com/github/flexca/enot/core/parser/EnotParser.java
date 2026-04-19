package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.parser.context.ParsingContext;
import com.github.flexca.enot.core.registry.EnotElementBodyResolver;
import com.github.flexca.enot.core.registry.EnotElementSpecification;
import com.github.flexca.enot.core.registry.EnotTypeSpecification;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.util.FormatUtils;
import com.github.flexca.enot.core.util.PlaceholderUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Parses eNot template JSON into a list of {@link EnotElement} instances.
 *
 * <p>The parser accepts a JSON object (single root element) or a JSON array
 * (multiple root elements) and walks the tree recursively, resolving element
 * types via the {@link com.github.flexca.enot.core.registry.EnotRegistry},
 * validating attributes and body structure, and delegating dynamic body
 * resolution to any registered {@link EnotElementBodyResolver}.</p>
 *
 * <p>Cyclic-dependency detection is handled transparently: a fresh
 * {@link ParsingContext} is created for each top-level {@link #parse(String, EnotContext)}
 * call, and a snapshot copy is passed to each body resolver so that sibling
 * branches do not interfere with each other's tracking sets.</p>
 *
 * <p>All parse errors are collected into {@link EnotJsonError} entries and
 * reported together via a single {@link EnotParsingException}, making it easy
 * to surface multiple problems in one pass.</p>
 */
public class EnotParser {

    public static final String ENOT_ELEMENT_TYPE_NAME = "type";
    public static final String ENOT_ELEMENT_OPTIONAL_NAME = "optional";
    public static final String ENOT_ELEMENT_ATTRIBUTES_NAME = "attributes";
    public static final String ENOT_ELEMENT_BODY_NAME = "body";

    private static final String COMMON_ERROR_MESSAGE = "Error during parsing of eNot, reason: ";

    private final EnotParserValidator parserValidator;
    private final ObjectMapper jsonObjectMapper;
    private final ObjectMapper yamlObjectMapper;

    public EnotParser(ObjectMapper jsonObjectMapper, ObjectMapper yamlObjectMapper) {
        this.parserValidator = new EnotParserValidator();
        this.jsonObjectMapper = jsonObjectMapper;
        this.yamlObjectMapper = yamlObjectMapper;
    }

    /**
     * Parses {@code json} into a list of {@link EnotElement} instances using a
     * fresh {@link ParsingContext}.
     *
     * <p>This is the standard entry point for top-level parsing. A new
     * {@link ParsingContext} is created automatically so that cyclic-dependency
     * detection starts from an empty state.</p>
     *
     * @param jsonOrYaml        the eNot template as a JSON or YAML string; must not be blank
     * @param enotContext the registry and shared services for this parse run
     * @return a non-empty list of parsed root elements
     * @throws EnotParsingException if the input is blank, not valid JSON, or
     *                              contains structural or type errors
     */
    public List<EnotElement> parse(String jsonOrYaml, EnotContext enotContext) throws EnotParsingException {
        ParsingContext parsingContext = new ParsingContext();
        return parse(jsonOrYaml, enotContext, parsingContext);
    }

    /**
     * Parses {@code json} into a list of {@link EnotElement} instances using the
     * supplied {@link ParsingContext}.
     *
     * <p>Use this overload when parsing is initiated from within an
     * {@link EnotElementBodyResolver} (e.g. {@code system/reference} resolution),
     * so that the caller's already-populated context — carrying the set of
     * composite identifiers currently being resolved — is passed through.
     * This allows cycle detection to span across template boundaries.</p>
     *
     * @param jsonOrYaml     the eNot template as a JSON or YAML string; must not be blank
     * @param enotContext    the registry and shared services for this parse run
     * @param parsingContext the active parsing context propagated from the caller;
     *                       must be a {@link ParsingContext#copy() copy} so that
     *                       sibling branches remain independent
     * @return a non-empty list of parsed root elements
     * @throws EnotParsingException if the input is blank, not valid JSON, or
     *                              contains structural or type errors
     */
    public List<EnotElement> parse(String jsonOrYaml, EnotContext enotContext, ParsingContext parsingContext) throws EnotParsingException {

        String currentPath = "";
        if (StringUtils.isBlank(jsonOrYaml)) {
            throw new EnotParsingException(COMMON_ERROR_MESSAGE,
                    Collections.singletonList(EnotJsonError.of(currentPath, "blank JSON input provided")));
        }

        List<EnotElement> elements = new ArrayList<>();
        JsonNode rootNode;

        EnotInputFormat inputFormat = FormatUtils.detectInputFormat(jsonOrYaml);
        if (EnotInputFormat.UNSUPPORTED.equals(inputFormat)) {
            throw new EnotParsingException(COMMON_ERROR_MESSAGE, Collections.singletonList(EnotJsonError.of(currentPath,
                    "unsupported input format, make sure you providing valid JSON or YAML")));
        }

        try {
            if(EnotInputFormat.JSON.equals(inputFormat)) {
                rootNode = jsonObjectMapper.readValue(jsonOrYaml, JsonNode.class);
            } else {
                rootNode = yamlObjectMapper.readValue(jsonOrYaml, JsonNode.class);
            }
        } catch(Exception e) {
            throw new EnotParsingException(COMMON_ERROR_MESSAGE,
                    Collections.singletonList(EnotJsonError.of(currentPath, e.getMessage())), e);
        }

        List<EnotJsonError> jsonErrors = new ArrayList<>();
        Throwable cause = null;

        if (rootNode.isArray()) {
            try {
                elements.addAll(parseElements(rootNode.asArray(), currentPath, jsonErrors, enotContext, parsingContext));
            } catch (Exception e) {
                cause = e;
                jsonErrors.add(EnotJsonError.of(currentPath, e.getMessage()));
            }
            if (elements.isEmpty() && jsonErrors.isEmpty()) {
                jsonErrors.add(EnotJsonError.of(currentPath, "no elements found"));
            }
        } else if (rootNode.isObject()) {
            try {
                Optional<EnotElement> element = parseElement(rootNode.asObject(), currentPath, jsonErrors, enotContext,
                        parsingContext);
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

    private List<EnotElement> parseElements(ArrayNode elementsArray, String parentPath, List<EnotJsonError> jsonErrors,
                                            EnotContext enotContext, ParsingContext parsingContext) {

        List<EnotElement> elements = new ArrayList<>(elementsArray.size());
        for (int i = 0; i < elementsArray.size(); i++) {
            JsonNode itemNode = elementsArray.get(i);
            String currentPath = parentPath + "/" + i;
            if (itemNode.isObject()) {
                Optional<EnotElement> element = parseElement(itemNode.asObject(), currentPath, jsonErrors, enotContext,
                        parsingContext);
                element.ifPresent(elements::add);
            } else {
                jsonErrors.add(EnotJsonError.of(currentPath, "eNot expecting object, but get " + itemNode.getNodeType().name()));
            }
        }
        return elements;
    }

    private Optional<EnotElement> parseElement(ObjectNode jsonElement, String parentPath, List<EnotJsonError> jsonErrors,
                                               EnotContext enotContext, ParsingContext parsingContext) {

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

        Optional<EnotTypeSpecification> typeSpecificationCandidate = enotContext.getEnotRegistry().getTypeSpecification(type);
        if (typeSpecificationCandidate.isEmpty()) {
            jsonErrors.add(EnotJsonError.of(typePath, "unsupported " + ENOT_ELEMENT_TYPE_NAME + " of eNot element: " + type
                    + ", make sure this type was added to EnotRegistry"));
            return Optional.empty();
        }

        EnotTypeSpecification typeSpecification = typeSpecificationCandidate.get();
        EnotElement element = new EnotElement();
        element.setType(typeSpecification.getTypeName());

        boolean optional = extractOptional(jsonElement, parentPath, jsonErrors);
        element.setOptional(optional);

        Map<EnotAttribute, Object> attributes = extractElementAttributes(jsonElement, typeSpecification, parentPath, jsonErrors);
        element.setAttributes(attributes);

        EnotElementSpecification elementSpecification = typeSpecification.getElementSpecification(element);
        if (elementSpecification == null) {
            jsonErrors.add(EnotJsonError.of(parentPath, "cannot find specification for element with attributes: "
                    + attributes));
            return Optional.empty();
        }
        EnotElementBodyResolver bodyResolver = elementSpecification.getBodyResolver();
        if (bodyResolver == null) {
            Optional<Object> elementBody = extractElementBody(jsonElement, parentPath, jsonErrors, enotContext, parsingContext);
            elementBody.ifPresent(element::setBody);
        } else {
            try {
                element.setBody(bodyResolver.resolveBody(element, enotContext, parsingContext.copy()));
            } catch(Exception e) {
                jsonErrors.add(EnotJsonError.of(parentPath + "/" + ENOT_ELEMENT_BODY_NAME,
                        "failure during resolving element body, reason: " + e.getMessage()));
                return Optional.empty();
            }
        }
        parserValidator.validateElement(typeSpecification, element, parentPath, jsonErrors, enotContext);
        typeSpecification.getElementValidator().validateElement(element, parentPath, jsonErrors, enotContext);

        return Optional.of(element);
    }

    private boolean extractOptional(ObjectNode jsonElement, String parentPath, List<EnotJsonError> jsonErrors) {

        JsonNode optionalNode = jsonElement.get(ENOT_ELEMENT_OPTIONAL_NAME);
        if(optionalNode == null) {
            return false;
        }
        String currentPath = parentPath + "/" + ENOT_ELEMENT_OPTIONAL_NAME;
        if(!optionalNode.isBoolean()) {
            jsonErrors.add(EnotJsonError.of(currentPath, "eNot element field " + ENOT_ELEMENT_OPTIONAL_NAME
                    + " if provided (by default is false) must be boolean, provided: " + optionalNode.getNodeType().name()));
            return false;
        }
        return optionalNode.asBoolean();
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
                jsonErrors.add(EnotJsonError.of(attributePath, "value for attribute " + attributeName + " must be set"));
                continue;
            }
            Optional<Object> attributeValue = extractAttributeValue(attribute, attributeValueNode, currentPath, jsonErrors);
            if (attributeValue.isPresent()) {
                attributes.put(attribute, attributeValue.get());
            }
        }
        return attributes;
    }

    private Optional<Object> extractElementBody(ObjectNode jsonElement, String parentPath, List<EnotJsonError> jsonErrors,
                                                EnotContext enotContext, ParsingContext parsingContext) {

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
                List<EnotElement> elements = parseElements(bodyArrayNode, currentPath, jsonErrors, enotContext, parsingContext);
                return CollectionUtils.isEmpty(elements) ? Optional.empty() : Optional.of(elements);
            } else {
                List<Object> primitiveValues = new ArrayList<>();
                int i = 0;
                for(JsonNode primitiveItem : bodyArrayNode) {
                    Optional<Object> objectBody = extractPrimitiveValue(primitiveItem, false, currentPath, jsonErrors);
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
            Optional<EnotElement> element = parseElement(bodyNode.asObject(), currentPath, jsonErrors, enotContext, parsingContext);
            return element.isEmpty() ? Optional.empty() : Optional.of(element.get());
        }  else {
            Optional<Object> objectBody = extractPrimitiveValue(bodyNode, false, currentPath, jsonErrors);
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
                Optional<Object> primitiveValue = extractPrimitiveValue(itemNode, false, parentPath, jsonErrors);
                if (primitiveValue.isEmpty()) {
                    jsonErrors.add(EnotJsonError.of(currentPath, "unexpected eNot attribute + " + attribute.getName() + " +  value JSON type: "
                            + itemNode.getNodeType().name() + ", expecting boolean, number or string"));
                } else {
                    attributeValues.add(primitiveValue.get());
                }
            }
            return attributeValues.isEmpty() ? Optional.empty() : Optional.of(attributeValues);
        } else {
            Optional<Object> primitiveValue = extractPrimitiveValue(valueNode, false, parentPath, jsonErrors);
            if (primitiveValue.isEmpty()) {
                jsonErrors.add(EnotJsonError.of(currentPath, "unexpected eNot attribute + " + attribute.getName() + " +  value JSON type: "
                        + valueNode.getNodeType().name() + ", expecting boolean, number or string"));
            }
            return primitiveValue;
        }
    }

    private Optional<Object> extractPrimitiveValue(JsonNode valueNode, boolean attributeValue, String currentPath,
                                                   List<EnotJsonError> jsonErrors) {

        if (valueNode.isString()) {
            String stringValue = valueNode.asString();
            if (!attributeValue) {
                Optional<String> variableName = PlaceholderUtils.extractPlaceholder(stringValue);
                if(variableName.isPresent() && !PlaceholderUtils.isValidVariableName(variableName.get())) {
                    jsonErrors.add(EnotJsonError.of(currentPath, "invalid variable name " + stringValue
                            + ", make sure you are using only letters, digits and underscores"));
                    return Optional.empty();
                }
            }
            return Optional.of(stringValue);
        } else if (valueNode.isBigDecimal()) {
            return Optional.of(valueNode.asDecimal());
        } else if (valueNode.isBigInteger()) {
            return Optional.of(valueNode.asBigInteger());
        } else if (valueNode.isInt()) {
            return Optional.of(valueNode.asInt());
        } else if (valueNode.isLong()) {
            return Optional.of(valueNode.asLong());
        } else if (valueNode.isBoolean()) {
            return Optional.of(valueNode.asBoolean());
        }  else {
            return Optional.empty();
        }
    }
}
