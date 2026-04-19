package com.github.flexca.enot.core;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.extractor.ExampleParamsExtractor;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.util.ParamUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Main entry point for the eNot library.
 *
 * <p>{@code Enot} ties together the parser, serializer, and example-parameter
 * extractor into a single, convenient facade. It is constructed exclusively via
 * {@link Builder}, which enforces that at least an {@link EnotRegistry} and one
 * {@link ObjectMapper} are configured before use.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * Enot enot = new Enot.Builder()
 *         .withRegistry(registry)
 *         .withJsonObjectMapper(new ObjectMapper())
 *         .build();
 *
 * List<EnotElement> elements = enot.parse(templateJson);
 * List<byte[]> der = enot.serialize(templateJson, serializationContext);
 * }</pre>
 *
 * <p>Instances are thread-safe after construction — all mutable state lives
 * inside the {@link SerializationContext} supplied by the caller at
 * serialization time.</p>
 */
public class Enot {

    private final EnotContext enotContext;
    private final EnotParser enotParser;
    private final EnotSerializer enotSerializer;
    private final ExampleParamsExtractor exampleParamsExtractor;
    private final ObjectMapper jsonObjectMapper;
    private final ObjectMapper yamlObjectMapper;

    private Enot(EnotRegistry enotRegistry, ObjectMapper jsonObjectMapper, ObjectMapper yamlObjectMapper) {

        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        ConditionExpressionEvaluator conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser);

        enotParser = new EnotParser(jsonObjectMapper, yamlObjectMapper);
        enotSerializer = new EnotSerializer(enotParser);
        enotContext = new EnotContext(enotRegistry, enotParser, enotSerializer, conditionExpressionParser, conditionExpressionEvaluator);
        exampleParamsExtractor = new ExampleParamsExtractor(enotContext);
        this.jsonObjectMapper = jsonObjectMapper;
        this.yamlObjectMapper = yamlObjectMapper;
    }

    /**
     * Parses an eNot template string into a list of {@link EnotElement} instances.
     *
     * <p>The input may be a JSON object (single root element), a JSON array
     * (multiple root elements), or equivalent YAML. Format is detected
     * automatically.</p>
     *
     * @param json the eNot template as a JSON or YAML string; must not be blank
     * @return a non-empty list of parsed root elements
     * @throws EnotParsingException if the input is blank, malformed, or contains
     *                              structural or type errors
     */
    public List<EnotElement> parse(String json) throws EnotParsingException {
        return enotParser.parse(json, enotContext);
    }

    /**
     * Parses an eNot template string and serializes all root elements to DER.
     *
     * <p>This is a convenience overload that combines {@link #parse(String)} and
     * {@link #serialize(List, SerializationContext)} in a single call.</p>
     *
     * @param json    the eNot template as a JSON or YAML string; must not be blank
     * @param context the serialization context supplying runtime parameter values
     * @return one DER-encoded byte array per root element
     * @throws EnotParsingException      if the template cannot be parsed
     * @throws EnotSerializationException if serialization of any element fails
     */
    public List<byte[]> serialize(String json, SerializationContext context) throws EnotParsingException, EnotSerializationException {
        return enotSerializer.serialize(json, context, enotContext);
    }

    /**
     * Serializes a single pre-parsed {@link EnotElement} to DER.
     *
     * @param element the element to serialize; must not be {@code null}
     * @param context the serialization context supplying runtime parameter values
     * @return a list containing one DER-encoded byte array
     * @throws EnotSerializationException if serialization fails
     */
    public List<byte[]> serialize(EnotElement element, SerializationContext context) throws EnotSerializationException {
        return enotSerializer.serialize(element, context, enotContext);
    }

    /**
     * Serializes a list of pre-parsed {@link EnotElement} instances to DER.
     *
     * @param elements the elements to serialize; must not be {@code null} or empty
     * @param context  the serialization context supplying runtime parameter values
     * @return one DER-encoded byte array per element
     * @throws EnotSerializationException if serialization of any element fails
     */
    public List<byte[]> serialize(List<EnotElement> elements, SerializationContext context) throws EnotSerializationException {
        return enotSerializer.serialize(elements, context, enotContext);
    }

    /**
     * Parses an eNot template and returns a JSON string containing example
     * parameter values for all placeholders found in the template.
     *
     * @param json the eNot template as a JSON or YAML string; must not be blank
     * @return a JSON object string mapping placeholder names to example values
     * @throws EnotParsingException if the template cannot be parsed
     */
    public String getParamsExampleJson(String json) throws EnotParsingException {
        return jsonObjectMapper.writeValueAsString(getParamsExample(json));
    }

    /**
     * Parses an eNot template and returns a map of example parameter values for
     * all placeholders found in the template.
     *
     * @param json the eNot template as a JSON or YAML string; must not be blank
     * @return a map from placeholder name to example value
     * @throws EnotParsingException if the template cannot be parsed
     */
    public Map<String, Object> getParamsExample(String json) throws EnotParsingException {
        List<EnotElement> elements = enotParser.parse(json, enotContext);
        return ParamUtils.toMap(exampleParamsExtractor.extractExampleParams(elements));
    }

    /**
     * Returns a JSON string containing example parameter values for all
     * placeholders found in the given pre-parsed {@link EnotElement}.
     *
     * @param element the element to extract parameters from; must not be {@code null}
     * @return a JSON object string mapping placeholder names to example values
     */
    public String getParamsExampleJson(EnotElement element) {
        return jsonObjectMapper.writeValueAsString(getParamsExample(element));
    }

    /**
     * Returns a map of example parameter values for all placeholders found in
     * the given pre-parsed {@link EnotElement}.
     *
     * @param element the element to extract parameters from; must not be {@code null}
     * @return a map from placeholder name to example value
     */
    public Map<String, Object> getParamsExample(EnotElement element) {
        return ParamUtils.toMap(exampleParamsExtractor.extractExampleParams(Collections.singletonList(element)));
    }

    /**
     * Returns a JSON string containing example parameter values for all
     * placeholders found across the given list of pre-parsed elements.
     *
     * @param elements the elements to extract parameters from; must not be {@code null}
     * @return a JSON object string mapping placeholder names to example values
     */
    public String getParamsExampleJson(List<EnotElement> elements) {
        return jsonObjectMapper.writeValueAsString(getParamsExample(elements));
    }

    /**
     * Returns a map of example parameter values for all placeholders found
     * across the given list of pre-parsed elements.
     *
     * @param elements the elements to extract parameters from; must not be {@code null}
     * @return a map from placeholder name to example value
     */
    public Map<String, Object> getParamsExample(List<EnotElement> elements) {
        return ParamUtils.toMap(exampleParamsExtractor.extractExampleParams(elements));
    }

    /**
     * Builder for {@link Enot}.
     *
     * <p>An {@link EnotRegistry} and at least one {@link ObjectMapper} (JSON or
     * YAML) must be provided before calling {@link #build()}.</p>
     */
    public static class Builder {

        private EnotRegistry enotRegistry;
        private ObjectMapper jsonObjectMapper;
        private ObjectMapper yamlObjectMapper;

        public Builder() {
        }

        /**
         * Sets the {@link EnotRegistry} that defines which element types and
         * type specifications are available during parsing and serialization.
         *
         * @param enotRegistry the registry to use; must not be {@code null}
         * @return this builder
         */
        public Builder withRegistry(EnotRegistry enotRegistry) {
            this.enotRegistry = enotRegistry;
            return this;
        }

        /**
         * Sets the {@link ObjectMapper} used to parse JSON input.
         *
         * <p>At least one of {@code withJsonObjectMapper} or
         * {@link #withYamlObjectMapper(ObjectMapper)} must be called before
         * {@link #build()}.</p>
         *
         * @param jsonObjectMapper a Jackson {@link ObjectMapper} configured for JSON
         * @return this builder
         */
        public Builder withJsonObjectMapper(ObjectMapper jsonObjectMapper) {
            this.jsonObjectMapper = jsonObjectMapper;
            return this;
        }

        /**
         * Sets the {@link ObjectMapper} used to parse YAML input.
         *
         * <p>At least one of {@link #withJsonObjectMapper(ObjectMapper)} or
         * {@code withYamlObjectMapper} must be called before {@link #build()}.</p>
         *
         * @param yamlObjectMapper a Jackson {@link ObjectMapper} configured for YAML
         *                         (typically created with {@code new ObjectMapper(new YAMLFactory())})
         * @return this builder
         */
        public Builder withYamlObjectMapper(ObjectMapper yamlObjectMapper) {
            this.yamlObjectMapper = yamlObjectMapper;
            return this;
        }

        /**
         * Builds and returns a fully initialised {@link Enot} instance.
         *
         * @return a new {@link Enot} instance
         * @throws EnotInvalidConfigurationException if {@code enotRegistry} is {@code null},
         *                                           or if neither a JSON nor a YAML
         *                                           {@link ObjectMapper} was provided
         */
        public Enot build() {

            if (enotRegistry == null) {
                throw new EnotInvalidConfigurationException("enotRegistry must be set");
            }

            if (jsonObjectMapper == null && yamlObjectMapper == null) {
                throw new EnotInvalidConfigurationException("at least one of jsonObjectMapper or yamlObjectMapper must be set");
            }

            return new Enot(enotRegistry, jsonObjectMapper, yamlObjectMapper);
        }
    }
}
