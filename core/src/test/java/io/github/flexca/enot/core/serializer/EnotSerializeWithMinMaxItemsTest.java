package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.exception.EnotSerializationException;
import io.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import io.github.flexca.enot.core.expression.ConditionExpressionParser;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EnotSerializeWithMinMaxItemsTest {

    private static final String TEMPLATE = "yaml/asn1/rfc/loop/extended-key-usage-with-min-max-items.yaml";

    // fixture has min_items=1, max_items=3

    private static final String EKU_SERVER_AUTH  = "1.3.6.1.5.5.7.3.1";
    private static final String EKU_CLIENT_AUTH  = "1.3.6.1.5.5.7.3.2";
    private static final String EKU_CODE_SIGNING = "1.3.6.1.5.5.7.3.3";
    private static final String EKU_EMAIL        = "1.3.6.1.5.5.7.3.4";

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

    private SerializationContext ctx(Map<String, Object> params) {
        return new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
    }

    // -----------------------------------------------------------------------
    // Success cases — item count within [min, max]
    // -----------------------------------------------------------------------

    @Test
    void testLoopWithExactlyMinItems() throws Exception {
        String template = ResourceReaderTestUtils.readResourceFileAsString(TEMPLATE);

        List<byte[]> result = enotSerializer.serialize(template, ctx(Map.of(
                "extended_key_usage", List.of(
                        Map.of("usage", EKU_SERVER_AUTH)   // exactly 1 == min_items
                )
        )), enotContext);

        assertThat(result).hasSize(1);
    }

    @Test
    void testLoopWithExactlyMaxItems() throws Exception {
        String template = ResourceReaderTestUtils.readResourceFileAsString(TEMPLATE);

        List<byte[]> result = enotSerializer.serialize(template, ctx(Map.of(
                "extended_key_usage", List.of(
                        Map.of("usage", EKU_SERVER_AUTH),
                        Map.of("usage", EKU_CLIENT_AUTH),
                        Map.of("usage", EKU_CODE_SIGNING)  // exactly 3 == max_items
                )
        )), enotContext);

        assertThat(result).hasSize(1);
    }

    @Test
    void testLoopWithItemCountBetweenMinAndMax() throws Exception {
        String template = ResourceReaderTestUtils.readResourceFileAsString(TEMPLATE);

        List<byte[]> result = enotSerializer.serialize(template, ctx(Map.of(
                "extended_key_usage", List.of(
                        Map.of("usage", EKU_SERVER_AUTH),
                        Map.of("usage", EKU_CLIENT_AUTH)   // 2 — within [1, 3]
                )
        )), enotContext);

        assertThat(result).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // Failure cases — item count violates min or max
    // -----------------------------------------------------------------------

    @Test
    void testLoopFailsWhenItemCountBelowMinItems() throws Exception {
        String template = ResourceReaderTestUtils.readResourceFileAsString(TEMPLATE);

        // 0 items < min_items=1
        assertThatThrownBy(() -> enotSerializer.serialize(template, ctx(Map.of(
                "extended_key_usage", List.of()
        )), enotContext))
                .isInstanceOf(EnotSerializationException.class)
                .satisfies(ex -> assertThat(((EnotSerializationException) ex).getJsonErrors())
                        .isNotEmpty()
                        .allSatisfy(err -> assertThat(err.getDetails()).contains("minimum")));
    }

    @Test
    void testLoopFailsWhenItemsParamAbsentAndMinItemsIsOne() throws Exception {
        String template = ResourceReaderTestUtils.readResourceFileAsString(TEMPLATE);

        // param missing entirely — resolves to empty → below min_items=1
        assertThatThrownBy(() -> enotSerializer.serialize(template, ctx(Map.of()), enotContext))
                .isInstanceOf(EnotSerializationException.class)
                .satisfies(ex -> assertThat(((EnotSerializationException) ex).getJsonErrors())
                        .isNotEmpty());
    }

    @Test
    void testLoopFailsWhenItemCountExceedsMaxItems() throws Exception {
        String template = ResourceReaderTestUtils.readResourceFileAsString(TEMPLATE);

        // 4 items > max_items=3
        assertThatThrownBy(() -> enotSerializer.serialize(template, ctx(Map.of(
                "extended_key_usage", List.of(
                        Map.of("usage", EKU_SERVER_AUTH),
                        Map.of("usage", EKU_CLIENT_AUTH),
                        Map.of("usage", EKU_CODE_SIGNING),
                        Map.of("usage", EKU_EMAIL)          // one too many
                )
        )), enotContext))
                .isInstanceOf(EnotSerializationException.class)
                .satisfies(ex -> assertThat(((EnotSerializationException) ex).getJsonErrors())
                        .isNotEmpty()
                        .allSatisfy(err -> assertThat(err.getDetails()).contains("maximum")));
    }
}
