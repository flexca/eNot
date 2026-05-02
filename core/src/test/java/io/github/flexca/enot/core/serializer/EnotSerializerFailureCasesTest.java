package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.exception.EnotParsingException;
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

public class EnotSerializerFailureCasesTest {

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
    // Required placeholder missing
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsWhenRequiredPlaceholderAbsent() throws Exception {

        // common_name is required (not optional), omitting it must throw
        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/subject-dn-common-name.json");

        assertThatThrownBy(() -> enotSerializer.serialize(json, ctx(Map.of()), enotContext))
                .isInstanceOf(EnotSerializationException.class);
    }

    @Test
    void testSerializationFailsWhenRequiredPlaceholderIsNull() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/subject-dn-common-name.json");

        Map<String, Object> params = new java.util.HashMap<>();
        params.put("common_name", null);

        assertThatThrownBy(() -> enotSerializer.serialize(json, ctx(params), enotContext))
                .isInstanceOf(EnotSerializationException.class);
    }

    // -----------------------------------------------------------------------
    // Wrong placeholder value type
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsWhenBooleanPlaceholderReceivesString() throws Exception {

        // key-usage expects boolean values for each bit; passing a string must throw
        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/extension-key-usage.json");

        assertThatThrownBy(() -> enotSerializer.serialize(json, ctx(Map.ofEntries(
                Map.entry("digital_signature",  "yes"),   // wrong type — must be boolean
                Map.entry("non_repudiation",    false),
                Map.entry("key_encipherment",   false),
                Map.entry("data_encipherment",  false),
                Map.entry("key_agreement",      false),
                Map.entry("key_cert_sign",      false),
                Map.entry("crl_sign",           false),
                Map.entry("encipher_only",      false),
                Map.entry("decipher_only",      false)
        )), enotContext))
                .isInstanceOf(EnotSerializationException.class);
    }

    // -----------------------------------------------------------------------
    // Condition expression — type mismatch at evaluation time
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsWhenConditionExpressionUsesWrongType() throws Exception {

        // x509-tbs-validity uses date_time() comparisons; passing a non-parseable
        // string value to a date_time() call must throw at evaluation time
        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/x509-tbs-validity.json");

        assertThatThrownBy(() -> enotSerializer.serialize(json, ctx(Map.of(
                "valid_from", "not-a-date",
                "expires_on", "2025-01-01T00:00:00Z"
        )), enotContext))
                .isInstanceOf(EnotSerializationException.class);
    }

    // -----------------------------------------------------------------------
    // Loop — items param absent
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsWhenLoopItemsParamAbsent() throws Exception {

        // The loop body contains required (non-optional) elements. When the
        // items param is absent the loop still iterates once with a null
        // context, and the required body placeholder resolves to null → throws.
        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/subject-dn-organizational-unit.json");

        assertThatThrownBy(() -> enotSerializer.serialize(json, ctx(Map.of()), enotContext))
                .isInstanceOf(EnotSerializationException.class);
    }

    @Test
    void testSerializationFailsWhenLoopItemsParamIsNotAList() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/subject-dn-organizational-unit.json");

        // passing a scalar instead of a list for the loop parameter must throw
        assertThatThrownBy(() -> enotSerializer.serialize(json, ctx(Map.of(
                "organizational_units", "not-a-list"
        )), enotContext))
                .isInstanceOf(EnotSerializationException.class);
    }

    // -----------------------------------------------------------------------
    // Invalid OID
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsWhenOidValueIsMalformed() {

        String malformedOid = """
                {
                  "type": "asn.1",
                  "attributes": { "tag": "object_identifier" },
                  "body": "not.a.valid.oid.!!!"
                }
                """;

        assertThatThrownBy(() -> enotSerializer.serialize(malformedOid, ctx(Map.of()), enotContext))
                .isInstanceOf(EnotParsingException.class);
    }

    // -----------------------------------------------------------------------
    // Inline template — unknown type
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsForUnknownElementType() {

        String unknownType = """
                {
                  "type": "unknown_type_xyz",
                  "attributes": {},
                  "body": "value"
                }
                """;

        // Unknown type is caught at parse time → EnotParsingException (which is also
        // a checked exception the caller must handle), but EnotSerializationException
        // is also acceptable if the type resolution is deferred to serialize time.
        assertThatThrownBy(() -> enotSerializer.serialize(unknownType, ctx(Map.of()), enotContext))
                .isInstanceOf(Exception.class)
                .satisfies(ex -> assertThat(ex)
                        .isInstanceOfAny(
                                io.github.flexca.enot.core.exception.EnotParsingException.class,
                                EnotSerializationException.class));
    }

    // -----------------------------------------------------------------------
    // Loop uniqueness — duplicate items rejected when uniqueness = enforce
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsWhenLoopUniquenessViolated() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("yaml/asn1/rfc/uniqueness/extended-key-usage-with-uniqueness.yaml");

        // duplicate OID values — must throw because uniqueness = enforce
        assertThatThrownBy(() -> enotSerializer.serialize(json, ctx(Map.of(
                "extended_key_usages", List.of(
                        Map.of("eku_oid", "1.3.6.1.5.5.7.3.1"),
                        Map.of("eku_oid", "1.3.6.1.5.5.7.3.1")  // duplicate
                )
        )), enotContext))
                .isInstanceOf(EnotSerializationException.class)
                .satisfies(ex -> assertThat(((EnotSerializationException) ex).getJsonErrors())
                        .isNotEmpty());
    }

    // -----------------------------------------------------------------------
    // Null template input
    // -----------------------------------------------------------------------

    @Test
    void testSerializationFailsForNullTemplate() {

        assertThatThrownBy(() -> enotSerializer.serialize((String) null, ctx(Map.of()), enotContext))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testSerializationFailsForBlankTemplate() {

        assertThatThrownBy(() -> enotSerializer.serialize("   ", ctx(Map.of()), enotContext))
                .isInstanceOf(Exception.class);
    }
}
