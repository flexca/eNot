package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import com.github.flexca.enot.core.testutil.TestResourcesReferenceResolver;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotSerializerReferenceResolverTest {

    private static final String SAN_OID = "2.5.29.17";

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

    private SerializationContext ctx(Map<String, Object> params) {
        return new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
    }

    // -----------------------------------------------------------------------
    // san-dns.json → LOOP producing one DERTaggedObject(implicit=2, IA5String) per dns entry
    // -----------------------------------------------------------------------

    @Test
    void testSerializeSanDnsLoop() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/san/san-dns.json");

        // Two DNS names → two tagged objects
        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "dns_name", List.of(
                        Map.of("value", "example.com"),
                        Map.of("value", "www.example.com")))), enotContext);

        assertThat(result).hasSize(2);

        // Each entry: context-specific primitive tag [2] containing an IA5String
        assertSanDnsEntry(result.get(0), "example.com");
        assertSanDnsEntry(result.get(1), "www.example.com");
    }

    @Test
    void testSerializeSanDnsLoopOptionalAbsent() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/san/san-dns.json");

        // No dns_name params → loop is optional → empty result
        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of()), enotContext);

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // extension-san.json → SEQUENCE { OID(2.5.29.17), OCTET_STRING { SEQUENCE { [2]dns... } } }
    //   san_critical omitted (optional) → only OID + OCTET_STRING in output
    // -----------------------------------------------------------------------

    @Test
    void testSerializeExtensionSanWithoutCritical() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/san/extension-san.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "san", Map.of(
                        "dns_name", List.of(
                                Map.of("value", "example.com"),
                                Map.of("value", "www.example.com"))))), enotContext);

        assertThat(result).hasSize(1);

        // Root: SEQUENCE
        ASN1Sequence root = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        // san_critical is optional and absent → 2 children: OID + OCTET_STRING
        assertThat(root.size()).isEqualTo(2);

        // [0] OID 2.5.29.17
        assertThat(((ASN1ObjectIdentifier) root.getObjectAt(0)).getId()).isEqualTo(SAN_OID);

        // [1] OCTET_STRING wrapping SEQUENCE { [2]"example.com", [2]"www.example.com" }
        ASN1OctetString octetString = (ASN1OctetString) root.getObjectAt(1);
        ASN1Sequence innerSeq = (ASN1Sequence) ASN1Primitive.fromByteArray(octetString.getOctets());
        assertThat(innerSeq.size()).isEqualTo(2);
        assertSanDnsEntry(innerSeq.getObjectAt(0).toASN1Primitive().getEncoded(), "example.com");
        assertSanDnsEntry(innerSeq.getObjectAt(1).toASN1Primitive().getEncoded(), "www.example.com");
    }

    @Test
    void testSerializeExtensionSanWithCritical() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/san/extension-san.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "san_critical", true,
                "san", Map.of(
                        "dns_name", List.of(
                                Map.of("value", "secure.example.com"))))), enotContext);

        assertThat(result).hasSize(1);

        // Root: SEQUENCE with 3 children: OID + BOOLEAN(true) + OCTET_STRING
        ASN1Sequence root = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        assertThat(root.size()).isEqualTo(3);

        assertThat(((ASN1ObjectIdentifier) root.getObjectAt(0)).getId()).isEqualTo(SAN_OID);
        // [1] BOOLEAN true (critical flag)
        assertThat(ASN1Primitive.fromByteArray(root.getObjectAt(1).toASN1Primitive().getEncoded()).toString())
                .isEqualTo("TRUE");

        // [2] OCTET_STRING wrapping SEQUENCE { [2]"secure.example.com" }
        ASN1OctetString octetString = (ASN1OctetString) root.getObjectAt(2);
        ASN1Sequence innerSeq = (ASN1Sequence) ASN1Primitive.fromByteArray(octetString.getOctets());
        assertThat(innerSeq.size()).isEqualTo(1);
        assertSanDnsEntry(innerSeq.getObjectAt(0).toASN1Primitive().getEncoded(), "secure.example.com");
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    /**
     * Asserts that the given DER blob is a context-specific implicit tag [2] (IA5String)
     * with the expected DNS name value — the encoding used for dNSName in SAN (RFC 5280).
     */
    private void assertSanDnsEntry(byte[] der, String expectedDnsName) throws Exception {
        ASN1TaggedObject tagged = (ASN1TaggedObject) ASN1Primitive.fromByteArray(der);
        assertThat(tagged.getTagNo()).isEqualTo(2);
        assertThat(DERIA5String.getInstance(tagged, false).getString()).isEqualTo(expectedDnsName);
    }
}
