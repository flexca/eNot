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
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotSerializerSuccessCasesTest {

    private static final String COMMON_NAME_OID    = "2.5.4.3";
    private static final String ORG_UNIT_OID       = "2.5.4.11";

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
        return new SerializationContext.Builder(jsonObjectMapper).withParams(params).build();
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
    // extension-key-usage.json  →  SEQUENCE { OID(2.5.29.15), [BOOLEAN], OCTET_STRING { BIT_STRING } }
    //
    // Template uses msb_first big_endian input order, matching RFC 5280 bit numbering:
    //   boolean[0]=digital_signature → named bit 0 → MSB of byte[0] (shift=7)
    //   boolean[2]=key_encipherment  → named bit 2 → shift=5
    //
    // For digital_signature=true, key_encipherment=true, all others false (9 bits):
    //   byte[0]: digital_sig(shift=7) | key_enc(shift=5) = 0x80|0x20 = 0xA0
    //   byte[1]: decipher_only=false → 0x00
    //   apply_padding=true: trim([0xA0,0x00])→[0xA0], padBits=5
    //   KeyUsage.digitalSignature=0x80, KeyUsage.keyEncipherment=0x20 → intValue=0xA0
    // -----------------------------------------------------------------------

    private static final String KEY_USAGE_OID = "2.5.29.15";

    @Test
    void testSerializeKeyUsageWithoutCritical() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/extension-key-usage.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "digital_signature", true,
                "non_repudiation",   false,
                "key_encipherment",  true,
                "data_encipherment", false,
                "key_agreement",     false,
                "key_cert_sign",     false,
                "crl_sign",          false,
                "encipher_only",     false,
                "decipher_only",     false
        )), enotContext);

        assertThat(result).hasSize(1);

        ASN1Sequence root = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        // critical is optional and absent → 2 children: OID + OCTET_STRING
        assertThat(root.size()).isEqualTo(2);

        assertThat(((ASN1ObjectIdentifier) root.getObjectAt(0)).getId()).isEqualTo(KEY_USAGE_OID);

        KeyUsage ku = parseKeyUsage(root.getObjectAt(1));
        assertThat(ku.hasUsages(KeyUsage.digitalSignature)).isTrue();
        assertThat(ku.hasUsages(KeyUsage.nonRepudiation)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyEncipherment)).isTrue();
        assertThat(ku.hasUsages(KeyUsage.dataEncipherment)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyAgreement)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyCertSign)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.cRLSign)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.encipherOnly)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.decipherOnly)).isFalse();
    }

    @Test
    void testSerializeKeyUsageWithCritical() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/extension-key-usage.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.ofEntries(
                Map.entry("key_usage_critical", true),
                Map.entry("digital_signature",  true),
                Map.entry("non_repudiation",    false),
                Map.entry("key_encipherment",   true),
                Map.entry("data_encipherment",  false),
                Map.entry("key_agreement",      false),
                Map.entry("key_cert_sign",      false),
                Map.entry("crl_sign",           false),
                Map.entry("encipher_only",      false),
                Map.entry("decipher_only",      false)
        )), enotContext);

        assertThat(result).hasSize(1);

        ASN1Sequence root = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        // critical present → 3 children: OID + BOOLEAN(true) + OCTET_STRING
        assertThat(root.size()).isEqualTo(3);

        assertThat(((ASN1ObjectIdentifier) root.getObjectAt(0)).getId()).isEqualTo(KEY_USAGE_OID);
        assertThat(ASN1Boolean.getInstance(root.getObjectAt(1)).isTrue()).isTrue();

        KeyUsage ku = parseKeyUsage(root.getObjectAt(2));
        assertThat(ku.hasUsages(KeyUsage.digitalSignature)).isTrue();
        assertThat(ku.hasUsages(KeyUsage.nonRepudiation)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyEncipherment)).isTrue();
        assertThat(ku.hasUsages(KeyUsage.dataEncipherment)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyAgreement)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyCertSign)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.cRLSign)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.encipherOnly)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.decipherOnly)).isFalse();
    }

    @Test
    void testSerializeKeyUsageWithDecipherOnly() throws Exception {
        // RFC 5280 §4.2.1.3: decipherOnly may only be asserted when keyAgreement is also set.
        // decipherOnly = (1 << 15) maps to named bit 8 → MSB of byte[1].
        // With msb_first big_endian and 9 input booleans:
        //   byte[0]: digital_sig(shift=7) | key_agreement(shift=3) = 0x88
        //   byte[1]: decipher_only(shift=7) = 0x80  (partial byte, padBits=7)

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/extension-key-usage.json");

        List<byte[]> result = enotSerializer.serialize(json, ctx(Map.of(
                "digital_signature", true,
                "non_repudiation",   false,
                "key_encipherment",  false,
                "data_encipherment", false,
                "key_agreement",     true,
                "key_cert_sign",     false,
                "crl_sign",          false,
                "encipher_only",     false,
                "decipher_only",     true
        )), enotContext);

        assertThat(result).hasSize(1);

        ASN1Sequence root = (ASN1Sequence) ASN1Primitive.fromByteArray(result.get(0));
        // critical is optional and absent → 2 children: OID + OCTET_STRING
        assertThat(root.size()).isEqualTo(2);

        assertThat(((ASN1ObjectIdentifier) root.getObjectAt(0)).getId()).isEqualTo(KEY_USAGE_OID);

        KeyUsage ku = parseKeyUsage(root.getObjectAt(1));
        assertThat(ku.hasUsages(KeyUsage.digitalSignature)).isTrue();
        assertThat(ku.hasUsages(KeyUsage.nonRepudiation)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyEncipherment)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.dataEncipherment)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.keyAgreement)).isTrue();
        assertThat(ku.hasUsages(KeyUsage.keyCertSign)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.cRLSign)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.encipherOnly)).isFalse();
        assertThat(ku.hasUsages(KeyUsage.decipherOnly)).isTrue();
    }

    /** Extracts the KeyUsage from an OCTET_STRING-wrapped DER BIT_STRING. */
    private KeyUsage parseKeyUsage(Object asn1Object) throws Exception {
        ASN1OctetString octetString = ASN1OctetString.getInstance(asn1Object);
        return KeyUsage.getInstance(ASN1Primitive.fromByteArray(octetString.getOctets()));
    }

    // -----------------------------------------------------------------------
    // bit_map — 4 combinations of bit_order × byte_order
    //
    // byte_order and bit_order describe the INPUT data layout.
    // Output is always big-endian MSB-first (standard Java / network byte order).
    //
    // Logical value: [0xFE, 0xCA]  (big-endian output)
    //   0xFE = 1111_1110,  0xCA = 1100_1010
    //
    // -----------------------------------------------------------------------

    @Test
    void testBitMapMsbFirstBigEndian() throws Exception {
        // Input already in big-endian MSB-first order — no reordering needed.
        // group[0]=0xFE=[1,1,1,1,1,1,1,0] → bytes[0]
        // group[1]=0xCA=[1,1,0,0,1,0,1,0] → bytes[1]
        List<Boolean> bits = List.of(
                true,  true, true,  true,  true, true,  true, false,
                true,  true, false, false, true, false, true, false);

        byte[] result = serializeBitMap("big_endian", "msb_first", bits);
        assertThat(result).isEqualTo(new byte[]{(byte) 0xFE, (byte) 0xCA});
    }

    @Test
    void testBitMapMsbFirstLittleEndian() throws Exception {
        // Input is little-endian (LSB group first) MSB-first within each byte.
        // Serializer reverses byte groups to produce big-endian output.
        // group[0]=0xCA=[1,1,0,0,1,0,1,0] → reversed → bytes[1]
        // group[1]=0xFE=[1,1,1,1,1,1,1,0] → reversed → bytes[0]
        List<Boolean> bits = List.of(
                true, true, false, false, true, false, true, false,
                true, true, true,  true,  true, true,  true, false);

        byte[] result = serializeBitMap("little_endian", "msb_first", bits);
        assertThat(result).isEqualTo(new byte[]{(byte) 0xFE, (byte) 0xCA});
    }

    @Test
    void testBitMapLsbFirstBigEndian() throws Exception {
        // Input is big-endian, LSB-first within each byte.
        // 0xFE=1111_1110: bit0=0,bit1=1..bit7=1 → [F,T,T,T,T,T,T,T]
        // 0xCA=1100_1010: bit0=0,bit1=1,bit2=0,bit3=1,bit4=0,bit5=0,bit6=1,bit7=1 → [F,T,F,T,F,F,T,T]
        List<Boolean> bits = List.of(
                false, true, true,  true, true, true,  true, true,
                false, true, false, true, false, false, true, true);

        byte[] result = serializeBitMap("big_endian", "lsb_first", bits);
        assertThat(result).isEqualTo(new byte[]{(byte) 0xFE, (byte) 0xCA});
    }

    @Test
    void testBitMapLsbFirstLittleEndian() throws Exception {
        // Input is little-endian LSB-first — both orderings reversed vs output.
        // group[0]=0xCA(LSB first)=[F,T,F,T,F,F,T,T] → reversed → bytes[1]
        // group[1]=0xFE(LSB first)=[F,T,T,T,T,T,T,T] → reversed → bytes[0]
        List<Boolean> bits = List.of(
                false, true, false, true, false, false, true, true,
                false, true, true,  true, true,  true,  true, true);

        byte[] result = serializeBitMap("little_endian", "lsb_first", bits);
        assertThat(result).isEqualTo(new byte[]{(byte) 0xFE, (byte) 0xCA});
    }

    @Test
    void testBitMapPartialByteSingleGroup() throws Exception {
        // 5 bits, big_endian msb_first: [1,0,1,1,0] packed from bit7 downward
        // = 1011_0000 = 0xB0, remaining 3 bits are zero
        List<Boolean> bits = List.of(true, false, true, true, false);

        byte[] result = serializeBitMap("big_endian", "msb_first", bits);
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(new byte[]{(byte) 0xB0});
    }

    @Test
    void testBitMapPartialByteAfterCompleteGroup() throws Exception {
        // 10 bits, big_endian msb_first:
        // group[0] = [1,1,1,1,1,1,1,0] = 0xFE (complete) → bytes[0]
        // partial   = [1,1]             = 1100_0000 = 0xC0  → bytes[1]
        List<Boolean> bits = List.of(
                true, true, true, true, true, true, true, false,
                true, true);

        byte[] result = serializeBitMap("big_endian", "msb_first", bits);
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(new byte[]{(byte) 0xFE, (byte) 0xC0});
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private byte[] serializeBitMap(String byteOrder, String bitOrder, List<Boolean> bits) throws Exception {
        // Build body as JSON array of individually named placeholders: ["${b0}", "${b1}", ...]
        // This avoids the ContextNode wrapping issue that occurs when a single placeholder
        // resolves to a List — individual primitive params unwrap cleanly.
        StringBuilder bodyArray = new StringBuilder("[");
        for (int i = 0; i < bits.size(); i++) {
            if (i > 0) bodyArray.append(",");
            bodyArray.append("\"${b").append(i).append("}\"");
        }
        bodyArray.append("]");

        String json = """
                {
                  "type": "system",
                  "attributes": {
                    "kind": "bit_map",
                    "byte_order": "%s",
                    "bit_order": "%s"
                  },
                  "body": %s
                }
                """.formatted(byteOrder, bitOrder, bodyArray);

        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < bits.size(); i++) {
            params.put("b" + i, bits.get(i));
        }

        List<byte[]> result = enotSerializer.serialize(json, ctx(params), enotContext);
        assertThat(result).hasSize(1);
        return result.get(0);
    }

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
