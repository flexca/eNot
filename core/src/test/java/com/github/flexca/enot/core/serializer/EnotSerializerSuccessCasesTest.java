package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotSerializerSuccessCasesTest {

    private static final String COMMON_NAME_OID    = "2.5.4.3";
    private static final String ORG_UNIT_OID       = "2.5.4.11";

    private ObjectMapper objectMapper = new ObjectMapper();

    private EnotContext enotContext;

    private EnotParser enotParser;

    private EnotSerializer enotSerializer;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .build();
        enotParser = new EnotParser(objectMapper);
        enotSerializer = new EnotSerializer(enotParser);
        ConditionExpressionParser expressionParser = new ConditionExpressionParser();
        ConditionExpressionEvaluator conditionExpressionEvaluator = new ConditionExpressionEvaluator(enotRegistry, expressionParser);
        enotContext = new EnotContext(enotRegistry, enotParser, enotSerializer, expressionParser, conditionExpressionEvaluator);

    }

    private SerializationContext ctx(Map<String, Object> params) {
        return new SerializationContext.Builder(objectMapper).withParams(params).build();
    }

    // -----------------------------------------------------------------------
    // subject-dn-common-name  →  SET { SEQUENCE { OID, UTF8String } }
    // -----------------------------------------------------------------------

    @Test
    void testSerializeCommonNameSuccess() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/subject-dn-common-name.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of("common_name", "Alice")), enotContext);

        assertThat(result).hasSize(1);

        // outer: SET
        ASN1Set set = (ASN1Set) ASN1Primitive.fromByteArray(result.get(0));
        assertThat(set.size()).isEqualTo(1);

        // inner: SEQUENCE { OID(2.5.4.3), UTF8String("Alice") }
        ASN1Sequence seq = (ASN1Sequence) set.getObjectAt(0);
        assertThat(seq.size()).isEqualTo(2);
        assertThat(((ASN1ObjectIdentifier) seq.getObjectAt(0)).getId()).isEqualTo(COMMON_NAME_OID);
        assertThat(((DERUTF8String) seq.getObjectAt(1)).getString()).isEqualTo("Alice");
    }

    // -----------------------------------------------------------------------
    // subject-dn-organizational-unit  →  per item: SET { SEQUENCE { OID, UTF8String } }
    // -----------------------------------------------------------------------

    @Test
    void testSerializeOrganizationalUnitSuccessSingleUnit() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/subject-dn-organizational-unit.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "organizational_units", List.of(Map.of("unit", "Engineering")))), enotContext);

        assertThat(result).hasSize(1);
        assertOrgUnitDer(result.get(0), "Engineering");
    }

    @Test
    void testSerializeOrganizationalUnitSuccessMultipleUnits() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/subject-dn-organizational-unit.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "organizational_units", List.of(
                        Map.of("unit", "Engineering"),
                        Map.of("unit", "Security"),
                        Map.of("unit", "Research")))), enotContext);

        assertThat(result).hasSize(3);
        assertOrgUnitDer(result.get(0), "Engineering");
        assertOrgUnitDer(result.get(1), "Security");
        assertOrgUnitDer(result.get(2), "Research");
    }

    // -----------------------------------------------------------------------
    // x509-tbs-validity  →  SEQUENCE { notBefore, notAfter }
    //   pre-2050  → UTCTime;   2050+ → GeneralizedTime  (per RFC 5280)
    // -----------------------------------------------------------------------

    @Test
    void testSerializeX509ValidityBothDatesPreY2050() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/x509-tbs-validity.json");

        // Both dates before 2050 → both encoded as UTCTime
        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "valid_from", "2024-01-15T10:00:00Z",
                "expires_on", "2025-01-15T10:00:00Z")), enotContext);

        assertThat(result).hasSize(1);

        ASN1Sequence seq = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        assertThat(seq.size()).isEqualTo(2);
        assertThat(seq.getObjectAt(0)).isInstanceOf(ASN1UTCTime.class);
        assertThat(seq.getObjectAt(1)).isInstanceOf(ASN1UTCTime.class);
        // ASN1UTCTime.getTime() returns "YYYYMMDDHHmmGMT+00:00" after adjustment
        assertThat(((ASN1UTCTime) seq.getObjectAt(0)).getTime()).startsWith("202401");
        assertThat(((ASN1UTCTime) seq.getObjectAt(1)).getTime()).startsWith("202501");
    }

    @Test
    void testSerializeX509ValidityBothDatesPostY2050() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/x509-tbs-validity.json");

        // Both dates on or after 2050 → both encoded as GeneralizedTime
        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "valid_from", "2050-06-01T00:00:00Z",
                "expires_on", "2051-06-01T00:00:00Z")), enotContext);

        assertThat(result).hasSize(1);

        ASN1Sequence seq = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        assertThat(seq.size()).isEqualTo(2);
        assertThat(seq.getObjectAt(0)).isInstanceOf(ASN1GeneralizedTime.class);
        assertThat(seq.getObjectAt(1)).isInstanceOf(ASN1GeneralizedTime.class);
        // ASN1GeneralizedTime.getTime() returns "YYYYMMDDHHmmss'Z'" raw string
        assertThat(((ASN1GeneralizedTime) seq.getObjectAt(0)).getTime()).startsWith("205006");
        assertThat(((ASN1GeneralizedTime) seq.getObjectAt(1)).getTime()).startsWith("205106");
    }

    @Test
    void testSerializeX509ValidityMixedDates() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/x509-tbs-validity.json");

        // valid_from pre-2050 → UTCTime,  expires_on post-2050 → GeneralizedTime
        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "valid_from", "2026-04-13T09:00:00Z",
                "expires_on", "2050-04-13T09:00:00Z")), enotContext);

        assertThat(result).hasSize(1);

        ASN1Sequence seq = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        assertThat(seq.size()).isEqualTo(2);
        assertThat(seq.getObjectAt(0)).isInstanceOf(ASN1UTCTime.class);
        assertThat(seq.getObjectAt(1)).isInstanceOf(ASN1GeneralizedTime.class);
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    /** Parses one DER blob and asserts SET { SEQUENCE { OID(2.5.4.11), UTF8String(expectedUnit) } } */
    private void assertOrgUnitDer(byte[] der, String expectedUnit) throws Exception {
        ASN1Set set = (ASN1Set) ASN1Primitive.fromByteArray(der);
        assertThat(set.size()).isEqualTo(1);
        ASN1Sequence seq = (ASN1Sequence) set.getObjectAt(0);
        assertThat(seq.size()).isEqualTo(2);
        assertThat(((ASN1ObjectIdentifier) seq.getObjectAt(0)).getId()).isEqualTo(ORG_UNIT_OID);
        assertThat(((DERUTF8String) seq.getObjectAt(1)).getString()).isEqualTo(expectedUnit);
    }
}
