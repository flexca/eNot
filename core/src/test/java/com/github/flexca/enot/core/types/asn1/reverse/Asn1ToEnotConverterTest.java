package com.github.flexca.enot.core.types.asn1.reverse;

import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERVisibleString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Asn1ToEnotConverterTest {

    private Asn1ToEnotConverter converter;

    @BeforeEach
    void init() {
        converter = new Asn1ToEnotConverter();
    }

    // -----------------------------------------------------------------------
    // Structural types
    // -----------------------------------------------------------------------

    @Test
    void testConvertSequence() throws Exception {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1ObjectIdentifier("1.2.3"));
        v.add(new DERUTF8String("hello"));
        EnotElement result = converter.toEnot(new DERSequence(v).getEncoded());

        assertAsn1Element(result, "sequence");
        assertThat(result.getBody()).isInstanceOf(List.class);
        List<?> body = (List<?>) result.getBody();
        assertThat(body).hasSize(2);
        assertAsn1Element((EnotElement) body.get(0), "object_identifier");
        assertAsn1Element((EnotElement) body.get(1), "utf8_string");
    }

    @Test
    void testConvertSet() throws Exception {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new DERUTF8String("value"));
        EnotElement result = converter.toEnot(new DERSet(v).getEncoded());

        assertAsn1Element(result, "set");
        assertThat(result.getBody()).isInstanceOf(List.class);
        assertThat((List<?>) result.getBody()).hasSize(1);
    }

    @Test
    void testConvertOctetStringWithNestedAsn1() throws Exception {
        // OCTET STRING wrapping a parseable ASN.1 structure → body is an EnotElement
        byte[] innerDer = new DERSequence(new ASN1ObjectIdentifier("1.2.3")).getEncoded();
        EnotElement result = converter.toEnot(new DEROctetString(innerDer).getEncoded());

        assertAsn1Element(result, "octet_string");
        assertThat(result.getBody()).isInstanceOf(EnotElement.class);
        assertAsn1Element((EnotElement) result.getBody(), "sequence");
    }

    @Test
    void testConvertOctetStringWithRawBytes() throws Exception {
        // OCTET STRING wrapping non-ASN.1 bytes → falls back to hex_to_bin system element
        byte[] rawBytes = new byte[]{0x01, 0x02, 0x03, 0x04};
        EnotElement result = converter.toEnot(new DEROctetString(rawBytes).getEncoded());

        assertAsn1Element(result, "octet_string");
        EnotElement hexToBin = (EnotElement) result.getBody();
        assertSystemElement(hexToBin, "hex_to_bin");
        assertThat(hexToBin.getBody()).isEqualTo(HexFormat.of().formatHex(rawBytes));
    }

    @Test
    void testConvertTaggedObjectExplicit() throws Exception {
        EnotElement result = converter.toEnot(
                new DERTaggedObject(true, 2, new DERUTF8String("san")).getEncoded());

        assertAsn1Element(result, "tagged_object");
        assertThat(result.getAttributes().get(Asn1Attribute.EXPLICIT)).isEqualTo(2);
        assertThat(result.getBody()).isInstanceOf(EnotElement.class);
        assertAsn1Element((EnotElement) result.getBody(), "utf8_string");
    }

    @Test
    void testConvertTaggedObjectImplicit() throws Exception {
        EnotElement result = converter.toEnot(
                new DERTaggedObject(false, 3, new DERUTF8String("san")).getEncoded());

        assertAsn1Element(result, "tagged_object");
        assertThat(result.getAttributes().get(Asn1Attribute.IMPLICIT)).isEqualTo(3);
    }

    // -----------------------------------------------------------------------
    // Leaf types
    // -----------------------------------------------------------------------

    @Test
    void testConvertObjectIdentifier() throws Exception {
        EnotElement result = converter.toEnot(new ASN1ObjectIdentifier("2.5.4.3").getEncoded());

        assertAsn1Element(result, "object_identifier");
        assertThat(result.getBody()).isEqualTo("2.5.4.3");
    }

    @Test
    void testConvertBoolean() throws Exception {
        EnotElement result = converter.toEnot(ASN1Boolean.TRUE.getEncoded());

        assertAsn1Element(result, "boolean");
        assertThat(result.getBody()).isEqualTo(true);
    }

    @Test
    void testConvertInteger() throws Exception {
        EnotElement result = converter.toEnot(new ASN1Integer(42).getEncoded());

        assertAsn1Element(result, "integer");
        assertThat(result.getBody()).isEqualTo("42");
    }

    @Test
    void testConvertUtf8String() throws Exception {
        EnotElement result = converter.toEnot(new DERUTF8String("hello").getEncoded());

        assertAsn1Element(result, "utf8_string");
        assertThat(result.getBody()).isEqualTo("hello");
    }

    @Test
    void testConvertPrintableString() throws Exception {
        EnotElement result = converter.toEnot(new DERPrintableString("Alice").getEncoded());

        assertAsn1Element(result, "printable_string");
        assertThat(result.getBody()).isEqualTo("Alice");
    }

    @Test
    void testConvertIa5String() throws Exception {
        EnotElement result = converter.toEnot(new DERIA5String("test@example.com").getEncoded());

        assertAsn1Element(result, "ia5_string");
        assertThat(result.getBody()).isEqualTo("test@example.com");
    }

    @Test
    void testConvertVisibleString() throws Exception {
        EnotElement result = converter.toEnot(new DERVisibleString("visible").getEncoded());

        assertAsn1Element(result, "visible_string");
        assertThat(result.getBody()).isEqualTo("visible");
    }

    @Test
    void testConvertBmpString() throws Exception {
        EnotElement result = converter.toEnot(new DERBMPString("bmp").getEncoded());

        assertAsn1Element(result, "bmp_string");
        assertThat(result.getBody()).isEqualTo("bmp");
    }

    @Test
    void testConvertBitString() throws Exception {
        byte[] bits = new byte[]{(byte) 0xA0};
        EnotElement result = converter.toEnot(new DERBitString(bits).getEncoded());

        assertAsn1Element(result, "bit_string");
        EnotElement hexToBin = (EnotElement) result.getBody();
        assertSystemElement(hexToBin, "hex_to_bin");
        assertThat(hexToBin.getBody()).isInstanceOf(String.class);
    }

    @Test
    void testConvertGeneralizedTime() throws Exception {
        EnotElement result = converter.toEnot(new DERGeneralizedTime("20300101000000Z").getEncoded());

        assertAsn1Element(result, "generalized_time");
        assertThat(result.getBody()).isEqualTo("2030-01-01T00:00:00Z");
    }

    @Test
    void testConvertUtcTime() throws Exception {
        EnotElement result = converter.toEnot(new DERUTCTime("300101000000Z").getEncoded());

        assertAsn1Element(result, "utc_time");
        assertThat(result.getBody()).isEqualTo("2030-01-01T00:00:00Z");
    }

    @Test
    void testConvertNull() throws Exception {
        EnotElement result = converter.toEnot(DERNull.INSTANCE.getEncoded());

        assertAsn1Element(result, "null");
        assertThat(result.getBody()).isNull();
    }

    // -----------------------------------------------------------------------
    // Base64 entry point
    // -----------------------------------------------------------------------

    @Test
    void testConvertFromBase64() throws Exception {
        byte[] der = new DERUTF8String("test").getEncoded();
        String base64 = Base64.getEncoder().encodeToString(der);

        EnotElement result = converter.toEnot(base64);

        assertAsn1Element(result, "utf8_string");
        assertThat(result.getBody()).isEqualTo("test");
    }

    // -----------------------------------------------------------------------
    // Failure cases
    // -----------------------------------------------------------------------

    @Test
    void testInvalidBase64ThrowsException() {
        assertThatThrownBy(() -> converter.toEnot("not-valid-base64!!!"))
                .isInstanceOf(EnotInvalidArgumentException.class)
                .hasMessageContaining("base64");
    }

    @Test
    void testInvalidAsn1BytesThrowsException() {
        assertThatThrownBy(() -> converter.toEnot(new byte[]{0x01, 0x02}))
                .isInstanceOf(EnotInvalidArgumentException.class)
                .hasMessageContaining("ASN.1 parsing");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void assertAsn1Element(EnotElement element, String expectedTag) {
        assertThat(element.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(element.isOptional()).isFalse();
        assertThat(element.getAttributes().get(Asn1Attribute.TAG)).isEqualTo(expectedTag);
    }

    private void assertSystemElement(EnotElement element, String expectedKind) {
        assertThat(element.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(element.isOptional()).isFalse();
        assertThat(element.getAttributes().get(SystemAttribute.KIND)).isEqualTo(expectedKind);
    }
}
