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
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
    void tagMismatchOnNonOptionalElementIsIgnored() throws Exception {
        List<EnotElement> template = parse(UTF8_STRING_TEMPLATE);
        // Provide an INTEGER where UTF8String is expected
        byte[] integerBinary = new ASN1Integer(1).getEncoded();

        assertThat(extractor.extractParams(template, integerBinary)).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Order independent search (CSR-like input)
    // -----------------------------------------------------------------------

    @Test
    void subjectRdnsInDifferentOrderAreMatchedByOid() throws Exception {
        List<EnotElement> template = new ArrayList<>();
        template.addAll(parse(rdnTemplate("2.5.4.3", "common_name")));
        template.addAll(parse(rdnTemplate("2.5.4.10", "organization")));
        template.addAll(parse(rdnTemplate("2.5.4.6", "country")));

        List<ASN1Encodable> rdns = List.of(
                rdn("2.5.4.6", new DERPrintableString("UA")),
                rdn("2.5.4.10", new DERUTF8String("Acme")),
                rdn("2.5.4.3", new DERUTF8String("example.com")));

        Map<String, Object> params = extractor.extractParams(template, rdns);

        assertThat(params)
                .containsEntry("common_name", "example.com")
                .containsEntry("organization", "Acme")
                .containsEntry("country", "UA")
                .hasSize(3);
    }

    @Test
    void unknownAndMissingRdnsAreIgnored() throws Exception {
        List<EnotElement> template = new ArrayList<>();
        template.addAll(parse(rdnTemplate("2.5.4.3", "common_name")));
        template.addAll(parse(rdnTemplate("2.5.4.10", "organization")));

        List<ASN1Encodable> rdns = List.of(
                rdn("2.5.4.11", new DERUTF8String("Unit")),
                rdn("2.5.4.3", new DERUTF8String("example.com")));

        Map<String, Object> params = extractor.extractParams(template, rdns);

        assertThat(params).containsOnlyKeys("common_name").containsEntry("common_name", "example.com");
    }

    @Test
    void loopCollectsAllMatchingElementsRegardlessOfPositionAndSkipsOthers() throws Exception {
        List<EnotElement> template = parse(LOOP_OF_SETS_TEMPLATE);

        ASN1EncodableVector outer = new ASN1EncodableVector();
        outer.add(rdn("2.5.4.3", new DERUTF8String("CN1")));
        outer.add(rdn("2.5.4.10", new DERUTF8String("Other")));
        outer.add(rdn("2.5.4.3", new DERUTF8String("CN2")));
        outer.add(rdn("2.5.4.3", new DERUTF8String("CN3")));

        Map<String, Object> params = extractor.extractParams(template, new DERSequence(outer).getEncoded());

        List<?> rdns = (List<?>) params.get("rdns");
        assertThat(rdns).hasSize(3);
        assertThat(((Map<?, ?>) rdns.get(0)).get("cn")).isEqualTo("CN1");
        assertThat(((Map<?, ?>) rdns.get(1)).get("cn")).isEqualTo("CN2");
        assertThat(((Map<?, ?>) rdns.get(2)).get("cn")).isEqualTo("CN3");
    }

    @Test
    void groupIsAbsentWhenNothingMatched() throws Exception {
        List<EnotElement> template = parse(GROUP_TEMPLATE);

        Map<String, Object> params = extractor.extractParams(template, new DERSequence().getEncoded());

        assertThat(params).doesNotContainKey("subject");
    }

    @Test
    void extensionIsFoundByOidAmongOtherExtensions() throws Exception {
        List<EnotElement> template = parse(sanExtensionTemplate());

        ASN1Encodable keyUsage = extension("2.5.29.15", false, new DERSequence(new DERUTF8String("ignored")));
        ASN1Encodable san = extension("2.5.29.17", true, new DERSequence(new ASN1Encodable[]{
                new DERTaggedObject(false, 1, new DERIA5String("mail@example.com")),
                new DERTaggedObject(false, 2, new DERIA5String("a.example.com")),
                new DERTaggedObject(false, 2, new DERIA5String("b.example.com"))}));

        Map<String, Object> params = extractor.extractParams(template, List.of(keyUsage, san));

        Map<?, ?> sanParams = (Map<?, ?>) params.get("san");
        assertThat(sanParams.get("critical")).isEqualTo(true);
        List<?> dnsNames = (List<?>) sanParams.get("dns_name");
        assertThat(dnsNames).hasSize(2);
        assertThat(((Map<?, ?>) dnsNames.get(0)).get("name")).isEqualTo("a.example.com");
        assertThat(((Map<?, ?>) dnsNames.get(1)).get("name")).isEqualTo("b.example.com");
    }

    @Test
    void extensionWithOtherOidIsNotMatched() throws Exception {
        List<EnotElement> template = parse(sanExtensionTemplate());

        ASN1Encodable keyUsage = extension("2.5.29.15", true, new DERSequence(new ASN1Encodable[]{
                new DERTaggedObject(false, 2, new DERIA5String("a.example.com"))}));

        assertThat(extractor.extractParams(template, List.of(keyUsage))).isEmpty();
    }

    @Test
    void roundTripKeyUsageBitMap() throws Exception {
        String template = """
                {
                  "type": "asn.1", "attributes": { "tag": "octet_string" },
                  "body": {
                    "type": "asn.1", "attributes": { "tag": "bit_string", "apply_padding": true },
                    "body": {
                      "type": "system", "attributes": { "kind": "bit_map", "byte_order": "big_endian", "bit_order": "msb_first" },
                      "body": ["${digital_signature}", "${non_repudiation}", "${key_encipherment}", "${data_encipherment}",
                               "${key_agreement}", "${key_cert_sign}", "${crl_sign}", "${encipher_only}", "${decipher_only}"]
                    }
                  }
                }
                """;
        Map<String, Object> expected = new java.util.LinkedHashMap<>();
        for (String name : List.of("digital_signature", "non_repudiation", "key_encipherment", "data_encipherment",
                "key_agreement", "key_cert_sign", "crl_sign", "encipher_only", "decipher_only")) {
            expected.put(name, false);
        }
        expected.put("digital_signature", true);
        expected.put("key_encipherment", true);
        expected.put("decipher_only", true);

        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(new ObjectMapper())
                .withParams(expected)
                .build();
        List<byte[]> serialized = enot.serialize(template, ctx);

        Map<String, Object> extracted = extractor.extractParams(enot.parse(template), serialized.get(0));

        assertThat(extracted).isEqualTo(expected);
    }

    @Test
    void roundTripIpAddressSan() throws Exception {
        String template = """
                {
                  "type": "asn.1", "attributes": { "tag": "tagged_object", "implicit": 7 },
                  "body": {
                    "type": "asn.1", "attributes": { "tag": "octet_string" },
                    "body": { "type": "system", "attributes": { "kind": "ip_address_to_bytes" }, "body": "${address}" }
                  }
                }
                """;

        for (String address : List.of("192.168.1.10", "2001:db8::1")) {
            SerializationContext ctx = new SerializationContext.Builder()
                    .withJsonObjectMapper(new ObjectMapper())
                    .withParam("address", address)
                    .build();
            List<byte[]> serialized = enot.serialize(template, ctx);

            Map<String, Object> extracted = extractor.extractParams(enot.parse(template), serialized.get(0));

            assertThat(java.net.InetAddress.getByName((String) extracted.get("address")))
                    .isEqualTo(java.net.InetAddress.getByName(address));
        }
    }

    @Test
    void hexToBinInsideOctetStringIsExtractedAsHexAndSystemVariablesAreSkipped() throws Exception {
        String template = """
                {
                  "type": "asn.1", "attributes": { "tag": "octet_string" },
                  "body": { "type": "system", "attributes": { "kind": "hex_to_bin" }, "body": "%s" }
                }
                """;
        byte[] binary = new DEROctetString(new byte[]{0x0a, 0x1b, 0x2c}).getEncoded();

        assertThat(extractor.extractParams(parse(template.formatted("${key_id}")), binary))
                .containsOnlyKeys("key_id").containsEntry("key_id", "0a1b2c");
        assertThat(extractor.extractParams(parse(template.formatted("${system.subject_key_identifier}")), binary))
                .isEmpty();
    }

    @Test
    void bitStringWithUnusedBitsDoesNotFail() throws Exception {
        List<EnotElement> template = parse("""
                { "type": "asn.1", "attributes": { "tag": "bit_string" }, "body": "${bits}" }
                """);
        byte[] binary = new DERBitString(new byte[]{(byte) 0xA8}, 3).getEncoded();

        assertThat(extractor.extractParams(template, binary)).containsEntry("bits", "a8");
    }

    @Test
    void inMemoryBouncyCastleObjectsAreSupported() throws Exception {
        // RDN is an ASN1Encodable that is not an ASN1Primitive, as returned by X500Name.toASN1Primitive()
        List<EnotElement> template = parse(rdnTemplate("2.5.4.3", "common_name"));
        X500Name subject = new X500Name("O=Acme,CN=example.com");
        ASN1Sequence subjectSequence = (ASN1Sequence) subject.toASN1Primitive();

        Map<String, Object> params = extractor.extractParams(template, Arrays.asList(subjectSequence.toArray()));

        assertThat(params).containsOnlyKeys("common_name").containsEntry("common_name", "example.com");
    }

    @Test
    void explicitlyTaggedValueIsUnwrapped() throws Exception {
        List<EnotElement> template = parse("""
                {
                  "type": "asn.1",
                  "attributes": { "tag": "tagged_object", "explicit": 3 },
                  "body": { "type": "asn.1", "attributes": { "tag": "utf8_string" }, "body": "${value}" }
                }
                """);

        Map<String, Object> params = extractor.extractParams(template,
                List.of(new DERTaggedObject(true, 1, new DERUTF8String("wrong tag")),
                        new DERTaggedObject(true, 3, new DERUTF8String("right tag"))));

        assertThat(params).containsOnlyKeys("value").containsEntry("value", "right tag");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String rdnTemplate(String oid, String placeholder) {
        return """
                {
                  "type": "asn.1", "optional": true, "attributes": { "tag": "set" },
                  "body": {
                    "type": "asn.1", "optional": true, "attributes": { "tag": "sequence" },
                    "body": [
                      { "type": "asn.1", "attributes": { "tag": "object_identifier" }, "body": "%s" },
                      { "type": "asn.1", "optional": true, "attributes": { "tag": "utf8_string" }, "body": "${%s}" }
                    ]
                  }
                }
                """.formatted(oid, placeholder);
    }

    private static String sanExtensionTemplate() {
        return """
                {
                  "type": "system", "attributes": { "kind": "group", "group_name": "san" },
                  "body": {
                    "type": "asn.1", "attributes": { "tag": "sequence" },
                    "body": [
                      { "type": "asn.1", "attributes": { "tag": "object_identifier" }, "body": "2.5.29.17" },
                      { "type": "asn.1", "optional": true, "attributes": { "tag": "boolean" }, "body": "${critical}" },
                      {
                        "type": "asn.1", "attributes": { "tag": "octet_string" },
                        "body": {
                          "type": "asn.1", "attributes": { "tag": "sequence" },
                          "body": {
                            "type": "system", "attributes": { "kind": "loop", "items_name": "dns_name" },
                            "body": {
                              "type": "asn.1", "attributes": { "tag": "tagged_object", "implicit": 2 },
                              "body": { "type": "asn.1", "attributes": { "tag": "ia5_string" }, "body": "${name}" }
                            }
                          }
                        }
                      }
                    ]
                  }
                }
                """;
    }

    private static ASN1Encodable rdn(String oid, ASN1Encodable value) {
        return new DERSet(new DERSequence(new ASN1Encodable[]{new ASN1ObjectIdentifier(oid), value}));
    }

    private static ASN1Encodable extension(String oid, boolean critical, ASN1Encodable value) throws Exception {
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1ObjectIdentifier(oid));
        if (critical) {
            vector.add(ASN1Boolean.TRUE);
        }
        vector.add(new DEROctetString(value.toASN1Primitive().getEncoded()));
        return new DERSequence(vector);
    }

    private List<EnotElement> parse(String json) {
        try {
            return enotParser.parse(json, enotContext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse template: " + e.getMessage(), e);
        }
    }
}
