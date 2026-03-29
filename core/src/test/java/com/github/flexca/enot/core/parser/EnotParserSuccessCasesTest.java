package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.system.SystemTypeSpecification;
import com.github.flexca.enot.core.system.attribute.SystemAttribute;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotParserSuccessCasesTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private EnotRegistry enotRegistry;

    private EnotParser enotParser;

    @BeforeEach
    void init() {
        enotRegistry = new EnotRegistry(new SystemTypeSpecification(), new Asn1TypeSpecification());
        enotParser = new EnotParser(enotRegistry, objectMapper);
    }

    @Test
    void testParseCommonNameJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/subject-dn-common-name.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "set"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sequenceElement = (EnotElement) root.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody()).isInstanceOf(List.class);

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(2);
        assertThat(sequenceBody.get(0)).isInstanceOf(EnotElement.class);
        assertThat(sequenceBody.get(1)).isInstanceOf(EnotElement.class);;

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat(oidElement.getBody()).isInstanceOf(String.class);
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.4.3");

        EnotElement utf8StringElement = (EnotElement) sequenceBody.get(1);
        assertThat(utf8StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(utf8StringElement.isOptional()).isFalse();
        assertThat(utf8StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utf8_string"));
        assertThat(utf8StringElement.getBody() instanceof String);
        assertThat((String) utf8StringElement.getBody()).isEqualTo("${common_name}");
    }

    @Test
    void testParseOrganizationalUnitJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/subject-dn-organizational-unit.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "loop", SystemAttribute.ITEMS_NAME, "organizational_units"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement setElement = (EnotElement) root.getBody();
        assertThat(setElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(setElement.isOptional()).isFalse();
        assertThat(setElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "set"));
        assertThat(setElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sequenceElement = (EnotElement) setElement.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody()).isInstanceOf(List.class);

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(2);

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.4.11");

        EnotElement utf8StringElement = (EnotElement) sequenceBody.get(1);
        assertThat(utf8StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(utf8StringElement.isOptional()).isFalse();
        assertThat(utf8StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utf8_string"));
        assertThat((String) utf8StringElement.getBody()).isEqualTo("${unit}");

    }

    @Test
    void testParseExtendedKeyUsageExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-extended-key-usage.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.37");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${extended_key_usage_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement innerSequenceElement = (EnotElement) octetStringElement.getBody();
        assertThat(innerSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(innerSequenceElement.isOptional()).isFalse();
        assertThat(innerSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(innerSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement loopElement = (EnotElement) innerSequenceElement.getBody();
        assertThat(loopElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(loopElement.isOptional()).isFalse();
        assertThat(loopElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "loop", SystemAttribute.ITEMS_NAME, "extended_key_usage"));
        assertThat(loopElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement usageOidElement = (EnotElement) loopElement.getBody();
        assertThat(usageOidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(usageOidElement.isOptional()).isFalse();
        assertThat(usageOidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) usageOidElement.getBody()).isEqualTo("${usage}");
    }

    @Test
    void testParseKeyUsageExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-key-usage.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.15");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${key_usage_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement bitStringElement = (EnotElement) octetStringElement.getBody();
        assertThat(bitStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(bitStringElement.isOptional()).isFalse();
        assertThat(bitStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "bit_string"));
        assertThat(bitStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement bitMapElement = (EnotElement) bitStringElement.getBody();
        assertThat(bitMapElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(bitMapElement.isOptional()).isFalse();
        assertThat(bitMapElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "bit_map", SystemAttribute.BYTE_ORDER, "little_endian", SystemAttribute.BIT_ORDER, "lsb_first"));
        assertThat(bitMapElement.getBody()).isInstanceOf(List.class);

        List<String> bitMapBody = (List<String>) bitMapElement.getBody();
        assertThat(bitMapBody).hasSize(9);
        assertThat(bitMapBody).containsExactly(
                "${key_usage.digital_signature}",
                "${key_usage.non_repudiation}",
                "${key_usage.key_encipherment}",
                "${key_usage.data_encipherment}",
                "${key_usage.key_agreement}",
                "${key_usage.key_cert_sign}",
                "${key_usage.crl_sign}",
                "${key_usage.encipher_only}",
                "${key_usage.decipher_only}"
        );
    }

    @Test
    void testParseCertificatePolicyExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-certificate-policy.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.32");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${certificate_policy.critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement outerSequenceElement = (EnotElement) octetStringElement.getBody();
        assertThat(outerSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(outerSequenceElement.isOptional()).isFalse();
        assertThat(outerSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(outerSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement optionalSequenceElement = (EnotElement) outerSequenceElement.getBody();
        assertThat(optionalSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(optionalSequenceElement.isOptional()).isTrue();
        assertThat(optionalSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(optionalSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement loopElement = (EnotElement) optionalSequenceElement.getBody();
        assertThat(loopElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(loopElement.isOptional()).isFalse();
        assertThat(loopElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.ITEMS_NAME, "certificate_policy", SystemAttribute.KIND, "loop"));
        assertThat(loopElement.getBody()).isInstanceOf(List.class);

        List<?> loopBody = (List<?>) loopElement.getBody();
        assertThat(loopBody).hasSize(1);
        assertThat(loopBody.get(0)).isInstanceOf(EnotElement.class);
    }

    @Test
    void testParseAuthorityKeyIdentifierExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-authority-key-identifier.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.35");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${authority_key_identifier_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sha1Element = (EnotElement) octetStringElement.getBody();
        assertThat(sha1Element.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(sha1Element.isOptional()).isFalse();
        assertThat(sha1Element.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "sha1"));
        assertThat(sha1Element.getBody()).isInstanceOf(EnotElement.class);

        EnotElement hexToBinElement = (EnotElement) sha1Element.getBody();
        assertThat(hexToBinElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(hexToBinElement.isOptional()).isFalse();
        assertThat(hexToBinElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "hex_to_bin"));
        assertThat((String) hexToBinElement.getBody()).isEqualTo("${issuer_public_key_hex}");
    }

    @Test
    void testParseX509TBSValidityJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/x509-tbs-validity.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(4);

        EnotElement validFromGeneralizedCondition = (EnotElement) rootBody.get(0);
        assertThat(validFromGeneralizedCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(validFromGeneralizedCondition.isOptional()).isFalse();
        assertThat(validFromGeneralizedCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "${valid_from} >= '2050-01-01T00:00:00Z'"));
        assertThat(validFromGeneralizedCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement validFromGeneralizedTime = (EnotElement) validFromGeneralizedCondition.getBody();
        assertThat(validFromGeneralizedTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(validFromGeneralizedTime.isOptional()).isFalse();
        assertThat(validFromGeneralizedTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "generalized_time"));
        assertThat((String) validFromGeneralizedTime.getBody()).isEqualTo("${valid_from}");

        EnotElement validFromUtcCondition = (EnotElement) rootBody.get(1);
        assertThat(validFromUtcCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(validFromUtcCondition.isOptional()).isFalse();
        assertThat(validFromUtcCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "${valid_from} < '2050-01-01T00:00:00Z'"));
        assertThat(validFromUtcCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement validFromUtcTime = (EnotElement) validFromUtcCondition.getBody();
        assertThat(validFromUtcTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(validFromUtcTime.isOptional()).isFalse();
        assertThat(validFromUtcTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utc_time"));
        assertThat((String) validFromUtcTime.getBody()).isEqualTo("${valid_from}");

        EnotElement expiresOnGeneralizedCondition = (EnotElement) rootBody.get(2);
        assertThat(expiresOnGeneralizedCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(expiresOnGeneralizedCondition.isOptional()).isFalse();
        assertThat(expiresOnGeneralizedCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "${expires_on} >= '2050-01-01T00:00:00Z'"));
        assertThat(expiresOnGeneralizedCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement expiresOnGeneralizedTime = (EnotElement) expiresOnGeneralizedCondition.getBody();
        assertThat(expiresOnGeneralizedTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(expiresOnGeneralizedTime.isOptional()).isFalse();
        assertThat(expiresOnGeneralizedTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "generalized_time"));
        assertThat((String) expiresOnGeneralizedTime.getBody()).isEqualTo("${expires_on}");

        EnotElement expiresOnUtcCondition = (EnotElement) rootBody.get(3);
        assertThat(expiresOnUtcCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(expiresOnUtcCondition.isOptional()).isFalse();
        assertThat(expiresOnUtcCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "${expires_on} < '2050-01-01T00:00:00Z'"));
        assertThat(expiresOnUtcCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement expiresOnUtcTime = (EnotElement) expiresOnUtcCondition.getBody();
        assertThat(expiresOnUtcTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(expiresOnUtcTime.isOptional()).isFalse();
        assertThat(expiresOnUtcTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utc_time"));
        assertThat((String) expiresOnUtcTime.getBody()).isEqualTo("${expires_on}");
    }
}
