package io.github.flexca.enot.core.types.asn1.reverse;

import io.github.flexca.enot.core.Enot;
import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import io.github.flexca.enot.core.expression.ConditionExpressionParser;
import io.github.flexca.enot.core.parser.EnotParser;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import io.github.flexca.enot.core.testutil.TestResourcesReferenceResolver;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Asn1ParamsExtractorTest {

    private static final String UTF8_STRING_TEMPLATE = """
            {
              "type": "asn.1",
              "attributes": { "tag": "utf8_string" },
              "body": "${name}"
            }
            """;

    private static final String SEQUENCE_TWO_STRINGS_TEMPLATE = """
            {
              "type": "asn.1",
              "attributes": { "tag": "sequence" },
              "body": [
                { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${first}" },
                { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${last}" }
              ]
            }
            """;

    private static final String SEQUENCE_WITH_OID_AND_STRING_TEMPLATE = """
            {
              "type": "asn.1",
              "attributes": { "tag": "sequence" },
              "body": [
                { "type": "asn.1", "attributes": { "tag": "object_identifier" }, "body": "2.5.4.3" },
                { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${common_name}" }
              ]
            }
            """;

    private static final String LOOP_OF_SETS_TEMPLATE = """
            {
              "type": "asn.1",
              "attributes": { "tag": "sequence" },
              "body": {
                "type": "system",
                "attributes": { "kind": "loop", "items_name": "rdns" },
                "body": {
                  "type": "asn.1",
                  "attributes": { "tag": "set" },
                  "body": {
                    "type": "asn.1",
                    "attributes": { "tag": "sequence" },
                    "body": [
                      { "type": "asn.1", "attributes": { "tag": "object_identifier" }, "body": "2.5.4.3" },
                      { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${cn}" }
                    ]
                  }
                }
              }
            }
            """;

    private static final String GROUP_TEMPLATE = """
            {
              "type": "asn.1",
              "attributes": { "tag": "sequence" },
              "body": [
                {
                  "type": "system",
                  "attributes": { "kind": "group", "group_name": "subject" },
                  "body": [
                    { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${first_name}" },
                    { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${last_name}" }
                  ]
                }
              ]
            }
            """;

    private static final String INTEGER_TEMPLATE = """
            {
              "type": "asn.1",
              "attributes": { "tag": "integer" },
              "body": "${version}"
            }
            """;

    private EnotParser enotParser;
    private EnotContext enotContext;
    private Asn1ParamsExtractor extractor;
    private Enot enot;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .withElementReferenceResolver(new TestResourcesReferenceResolver())
                .build();
        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        enotParser = new EnotParser(jsonMapper, yamlMapper);
        enotContext = new EnotContext(enotRegistry, enotParser, new EnotSerializer(enotParser),
                conditionExpressionParser, new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser));
        extractor = new Asn1ParamsExtractor();

        enot = new Enot.Builder()
                .withRegistry(enotRegistry)
                .withJsonObjectMapper(jsonMapper)
                .withYamlObjectMapper(yamlMapper)
                .build();
    }

    // -----------------------------------------------------------------------
    // Leaf types
    // -----------------------------------------------------------------------

    @Test
    void extractsStringValueFromUtf8String() throws Exception {
        List<EnotElement> template = enotParser.parse(UTF8_STRING_TEMPLATE, enotContext);
        byte[] binary = new DERUTF8String("Alice").getEncoded();

        Map<String, Object> params = extractor.extractParams(template, binary);

        assertThat(params).containsEntry("name", "Alice");
    }

    @Test
    void extractsStringValueFromInteger() throws Exception {
        List<EnotElement> template = enotParser.parse(INTEGER_TEMPLATE, enotContext);
        byte[] binary = new ASN1Integer(42).getEncoded();

        Map<String, Object> params = extractor.extractParams(template, binary);

        assertThat(params).containsEntry("version", "42");
    }

    // -----------------------------------------------------------------------
    // Container types
    // -----------------------------------------------------------------------

    @Test
    void extractsMultipleValuesFromSequence() throws Exception {
        List<EnotElement> template = enotParser.parse(SEQUENCE_TWO_STRINGS_TEMPLATE, enotContext);

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DERUTF8String("John"));
        v.add(new DERUTF8String("Doe"));
        byte[] binary = new DERSequence(v).getEncoded();

        Map<String, Object> params = extractor.extractParams(template, binary);

        assertThat(params)
                .containsEntry("first", "John")
                .containsEntry("last", "Doe");
    }

    @Test
    void literalBodyInSequenceIsNotExtracted() throws Exception {
        // The OID body "2.5.4.3" is a literal, not a placeholder — only ${common_name} should appear
        List<EnotElement> template = enotParser.parse(SEQUENCE_WITH_OID_AND_STRING_TEMPLATE, enotContext);

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1ObjectIdentifier("2.5.4.3"));
        v.add(new DERUTF8String("Alice"));
        byte[] binary = new DERSequence(v).getEncoded();

        Map<String, Object> params = extractor.extractParams(template, binary);

        assertThat(params).containsOnlyKeys("common_name");
        assertThat(params).containsEntry("common_name", "Alice");
    }

    // -----------------------------------------------------------------------
    // LOOP
    // -----------------------------------------------------------------------

    @Test
    void extractsListParamsFromLoop() throws Exception {
        List<EnotElement> template = enotParser.parse(LOOP_OF_SETS_TEMPLATE, enotContext);

        // Build: SEQUENCE { SET { SEQUENCE { OID, "CN1" } }, SET { SEQUENCE { OID, "CN2" } } }
        ASN1EncodableVector rdn1 = new ASN1EncodableVector();
        rdn1.add(new ASN1ObjectIdentifier("2.5.4.3"));
        rdn1.add(new DERUTF8String("CN1"));

        ASN1EncodableVector rdn2 = new ASN1EncodableVector();
        rdn2.add(new ASN1ObjectIdentifier("2.5.4.3"));
        rdn2.add(new DERUTF8String("CN2"));

        ASN1EncodableVector outer = new ASN1EncodableVector();
        outer.add(new DERSet(new DERSequence(rdn1)));
        outer.add(new DERSet(new DERSequence(rdn2)));
        byte[] binary = new DERSequence(outer).getEncoded();

        Map<String, Object> params = extractor.extractParams(template, binary);

        assertThat(params).containsKey("rdns");
        List<?> rdns = (List<?>) params.get("rdns");
        assertThat(rdns).hasSize(2);
        Map<?, ?> rdn0 = (Map<?, ?>) rdns.get(0);
        Map<?, ?> rdnLast = (Map<?, ?>) rdns.get(1);
        assertThat(rdn0.get("cn")).isEqualTo("CN1");
        assertThat(rdnLast.get("cn")).isEqualTo("CN2");
    }

    @Test
    void emptyLoopWhenNoBinaryChildrenPresent() throws Exception {
        List<EnotElement> template = enotParser.parse(LOOP_OF_SETS_TEMPLATE, enotContext);

        // Empty sequence — no SET children → loop produces empty list
        byte[] binary = new DERSequence().getEncoded();

        Map<String, Object> params = extractor.extractParams(template, binary);

        assertThat(params).containsKey("rdns");
        assertThat((List<?>) params.get("rdns")).isEmpty();
    }

    // -----------------------------------------------------------------------
    // GROUP
    // -----------------------------------------------------------------------

    @Test
    void extractsNestedParamsFromGroup() throws Exception {
        List<EnotElement> template = enotParser.parse(GROUP_TEMPLATE, enotContext);

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DERUTF8String("Jane"));
        v.add(new DERUTF8String("Smith"));
        byte[] binary = new DERSequence(v).getEncoded();

        Map<String, Object> params = extractor.extractParams(template, binary);

        assertThat(params).containsKey("subject");
        Map<?, ?> subject = (Map<?, ?>) params.get("subject");
        assertThat(subject.get("first_name")).isEqualTo("Jane");
        assertThat(subject.get("last_name")).isEqualTo("Smith");
    }

    // -----------------------------------------------------------------------
    // Round-trip via Enot facade
    // -----------------------------------------------------------------------

    @Test
    void roundTripSingleStringValue() throws Exception {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(new ObjectMapper())
                .withParam("name", "Alice")
                .build();
        List<byte[]> serialized = enot.serialize(UTF8_STRING_TEMPLATE, ctx);

        Map<String, Object> extracted = extractor.extractParams(enot.parse(UTF8_STRING_TEMPLATE), serialized.get(0));

        assertThat(extracted).containsEntry("name", "Alice");
    }

    @Test
    void roundTripTwoStringValues() throws Exception {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(new ObjectMapper())
                .withParam("first", "Bob")
                .withParam("last", "Builder")
                .build();
        List<byte[]> serialized = enot.serialize(SEQUENCE_TWO_STRINGS_TEMPLATE, ctx);

        Map<String, Object> extracted = extractor.extractParams(enot.parse(SEQUENCE_TWO_STRINGS_TEMPLATE), serialized.get(0));

        assertThat(extracted)
                .containsEntry("first", "Bob")
                .containsEntry("last", "Builder");
    }

    @Test
    void roundTripLoopElements() throws Exception {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(new ObjectMapper())
                .withParams(Map.of("rdns", List.of(Map.of("cn", "example.com"), Map.of("cn", "www.example.com"))))
                .build();
        List<byte[]> serialized = enot.serialize(LOOP_OF_SETS_TEMPLATE, ctx);

        Map<String, Object> extracted = extractor.extractParams(enot.parse(LOOP_OF_SETS_TEMPLATE), serialized.get(0));

        List<?> rdns = (List<?>) extracted.get("rdns");
        assertThat(rdns).hasSize(2);
        Map<?, ?> rdn0 = (Map<?, ?>) rdns.get(0);
        Map<?, ?> rdn1 = (Map<?, ?>) rdns.get(1);
        assertThat(rdn0.get("cn")).isEqualTo("example.com");
        assertThat(rdn1.get("cn")).isEqualTo("www.example.com");
    }

    @Test
    void roundTripGroupElement() throws Exception {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(new ObjectMapper())
                .withParams(Map.of("subject", Map.of("first_name", "Alice", "last_name", "Wonder")))
                .build();
        List<byte[]> serialized = enot.serialize(GROUP_TEMPLATE, ctx);

        Map<String, Object> extracted = extractor.extractParams(enot.parse(GROUP_TEMPLATE), serialized.get(0));

        Map<?, ?> subject = (Map<?, ?>) extracted.get("subject");
        assertThat(subject.get("first_name")).isEqualTo("Alice");
        assertThat(subject.get("last_name")).isEqualTo("Wonder");
    }

    @Test
    void roundTripSubjectDnFromResourceTemplate() throws Exception {
        String template = new String(
                ResourceReaderTestUtils.readResourceFile("json/asn1/rfc/subject-dn-common-name.json"));

        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(new ObjectMapper())
                .withParam("common_name", "example.com")
                .build();
        List<byte[]> serialized = enot.serialize(template, ctx);

        Map<String, Object> extracted = extractor.extractParams(enot.parse(template), serialized.get(0));

        assertThat(extracted).containsEntry("common_name", "example.com");
    }

    // -----------------------------------------------------------------------
    // Error handling
    // -----------------------------------------------------------------------

    @Test
    void invalidBase64ThrowsEnotInvalidArgumentException() {
        List<EnotElement> template = parse(UTF8_STRING_TEMPLATE);

        assertThatThrownBy(() -> extractor.extractParams(template, "NOT!VALID!BASE64!!!"))
                .isInstanceOf(EnotInvalidArgumentException.class)
                .hasMessageContaining("base64");
    }

    @Test
    void invalidBinaryThrowsEnotInvalidArgumentException() {
        List<EnotElement> template = parse(UTF8_STRING_TEMPLATE);

        assertThatThrownBy(() -> extractor.extractParams(template, new byte[]{0x00, 0x01, 0x02}))
                .isInstanceOf(EnotInvalidArgumentException.class)
                .hasMessageContaining("parsing");
    }

    @Test
    void tagMismatchOnNonOptionalElementThrows() {
        List<EnotElement> template = parse(UTF8_STRING_TEMPLATE);
        // Provide an INTEGER where UTF8String is expected
        byte[] integerBinary;
        try {
            integerBinary = new ASN1Integer(1).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThatThrownBy(() -> extractor.extractParams(template, integerBinary))
                .isInstanceOf(EnotInvalidArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private List<EnotElement> parse(String json) {
        try {
            return enotParser.parse(json, enotContext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse template: " + e.getMessage(), e);
        }
    }
}
