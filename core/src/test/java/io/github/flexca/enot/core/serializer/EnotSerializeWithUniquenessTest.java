package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.exception.EnotException;
import io.github.flexca.enot.core.exception.EnotParsingException;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import io.github.flexca.enot.core.expression.ConditionExpressionParser;
import io.github.flexca.enot.core.parser.EnotJsonError;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnotSerializeWithUniquenessTest {

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    private EnotContext enotContext;

    private EnotParser enotParser;

    private EnotSerializer enotSerializer;

    @BeforeEach
    void init() {

        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .build();
        enotParser = new EnotParser(jsonObjectMapper, yamlObjectMapper);
        enotSerializer = new EnotSerializer(enotParser);
        ConditionExpressionParser expressionParser = new ConditionExpressionParser();
        ConditionExpressionEvaluator conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, expressionParser);
        enotContext = new EnotContext(enotRegistry, enotParser, enotSerializer, expressionParser, conditionExpressionEvaluator);
    }

    @Test
    void testExtendedKeyUsageUniqueValues() throws Exception {

        String template = ResourceReaderTestUtils.readResourceFileAsString("yaml/asn1/rfc/uniqueness/extended-key-usage-with-uniqueness.yaml");

        String params = """
                extended_key_usage_critical: true
                extended_key_usage:
                - usage: "1.3.6.1.5.5.7.3.1"
                - usage: "1.3.6.1.5.5.7.3.2"
                """;

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();

        List<byte[]> actual = enotSerializer.serialize(template, serializationContext, enotContext);

        assertThat(actual).isNotNull();
    }

    @Test
    void testExtendedKeyUsageNotUniqueValues() throws Exception {

        String template = ResourceReaderTestUtils.readResourceFileAsString("yaml/asn1/rfc/uniqueness/extended-key-usage-with-uniqueness.yaml");

        String params = """
                extended_key_usage_critical: true
                extended_key_usage:
                - usage: "1.3.6.1.5.5.7.3.1"
                - usage: "1.3.6.1.5.5.7.3.1"
                """;

        SerializationContext serializationContext = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();

        EnotSerializationException serializationException = assertThrows(EnotSerializationException.class, () -> {
            enotSerializer.serialize(template, serializationContext, enotContext);
        });

        List<EnotJsonError> jsonErrors = serializationException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
    }
}
