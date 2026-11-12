package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import io.github.flexca.enot.core.expression.ConditionExpressionParser;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.testutil.TestResourcesReferenceResolver;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotSerializerConditionsTest {

    private static final String SAN_DNS_WITH_CONDITION = """
            type: "system"
            attributes:
              kind: "condition"
              expression: "!is_null(${dns_name}) && length(${dns_name}) > 0"
            body:
              type: "system"
              attributes:
                kind: "loop"
                items_name: "dns_name"
              body:
                type: "asn.1"
                attributes:
                  tag: "tagged_object"
                  implicit: 2
                body:
                  type: "asn.1"
                  attributes:
                    tag: "ia5_string"
                  body: "${name}"
            """;

    private static final String COMMON_NAME_WITH_IS_BLANK_CONDITION = """
            type: "system"
            attributes:
              kind: "condition"
              expression: "!is_blank(${common_name})"
            body:
              type: "asn.1"
              attributes:
                tag: "utf8_string"
              body: "${common_name}"
            """;

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    private EnotContext enotContext;

    private EnotParser enotParser;

    private EnotSerializer enotSerializer;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .withElementReferenceResolver(new TestResourcesReferenceResolver())
                .build();
        enotParser = new EnotParser(jsonObjectMapper, yamlObjectMapper);
        enotSerializer = new EnotSerializer(enotParser);
        ConditionExpressionParser expressionParser = new ConditionExpressionParser();
        ConditionExpressionEvaluator conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, expressionParser);
        enotContext = new EnotContext(enotRegistry, enotParser, enotSerializer, expressionParser, conditionExpressionEvaluator);
    }

    @Test
    void testConditionSanTrue() throws Exception {

        String params = """
                dns_name:
                - name: "replace with your value"
                """;

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
        List<byte[]> result = enotSerializer.serialize(SAN_DNS_WITH_CONDITION, serializationContext, enotContext);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void testConditionSanFalse() throws Exception {

        String params = """
                dns_name: []
                """;

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
        List<byte[]> result = enotSerializer.serialize(SAN_DNS_WITH_CONDITION, serializationContext, enotContext);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testConditionIsBlankTrue() throws Exception {

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams("common_name: \"Alice\"")
                .build();
        List<byte[]> result = enotSerializer.serialize(COMMON_NAME_WITH_IS_BLANK_CONDITION, serializationContext, enotContext);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void testConditionIsBlankFalseOnBlankValue() throws Exception {

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams("common_name: \"   \"")
                .build();
        List<byte[]> result = enotSerializer.serialize(COMMON_NAME_WITH_IS_BLANK_CONDITION, serializationContext, enotContext);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testConditionIsBlankFalseOnMissingValue() throws Exception {

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withYamlObjectMapper(yamlObjectMapper)
                .build();
        List<byte[]> result = enotSerializer.serialize(COMMON_NAME_WITH_IS_BLANK_CONDITION, serializationContext, enotContext);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
