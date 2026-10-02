package io.github.flexca.enot.core.parser;

import io.github.flexca.enot.core.EnotContext;
import io.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import io.github.flexca.enot.core.expression.ConditionExpressionParser;
import io.github.flexca.enot.core.serializer.EnotSerializer;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import io.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import io.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotParserSuccessCasesTest {

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    private EnotContext enotContext;

    private EnotParser enotParser;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .build();
        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        enotParser = new EnotParser(jsonObjectMapper, yamlObjectMapper);
        enotContext = new EnotContext(enotRegistry, enotParser, new EnotSerializer(enotParser),
                conditionExpressionParser, new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser));

    }

    @Test
    void testParseCommonNameJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/subject-dn-common-name.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "set"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sequenceElement = (EnotElement) root.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.getJsonPath()).isEqualTo("/body");
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody()).isInstanceOf(List.class);

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(2);
        assertThat(sequenceBody.get(0)).isInstanceOf(EnotElement.class);
        assertThat(sequenceBody.get(1)).isInstanceOf(EnotElement.class);;

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat(oidElement.getBody()).isInstanceOf(String.class);
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.4.3");

        EnotElement utf8StringElement = (EnotElement) sequenceBody.get(1);
        assertThat(utf8StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(utf8StringElement.getJsonPath()).isEqualTo("/body/body/1");
        assertThat(utf8StringElement.isOptional()).isFalse();
        assertThat(utf8StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utf8_string"));
        assertThat(utf8StringElement.getBody() instanceof String);
        assertThat((String) utf8StringElement.getBody()).isEqualTo("${common_name}");
    }

    @Test
    void testParseOrganizationalUnitJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/subject-dn-organizational-unit.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "loop", SystemAttribute.ITEMS_NAME, "organizational_units"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement setElement = (EnotElement) root.getBody();
        assertThat(setElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(setElement.getJsonPath()).isEqualTo("/body");
        assertThat(setElement.isOptional()).isFalse();
        assertThat(setElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "set"));
        assertThat(setElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sequenceElement = (EnotElement) setElement.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.getJsonPath()).isEqualTo("/body/body");
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody()).isInstanceOf(List.class);

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(2);

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/body/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.4.11");

        EnotElement utf8StringElement = (EnotElement) sequenceBody.get(1);
        assertThat(utf8StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(utf8StringElement.getJsonPath()).isEqualTo("/body/body/body/1");
        assertThat(utf8StringElement.isOptional()).isFalse();
        assertThat(utf8StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utf8_string"));
        assertThat((String) utf8StringElement.getBody()).isEqualTo("${unit}");

    }

    @Test
    void testParseExtendedKeyUsageExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-extended-key-usage.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.37");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${extended_key_usage_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement innerSequenceElement = (EnotElement) octetStringElement.getBody();
        assertThat(innerSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(innerSequenceElement.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(innerSequenceElement.isOptional()).isFalse();
        assertThat(innerSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(innerSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement loopElement = (EnotElement) innerSequenceElement.getBody();
        assertThat(loopElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(loopElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(loopElement.isOptional()).isFalse();
        assertThat(loopElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "loop", SystemAttribute.ITEMS_NAME, "extended_key_usage"));
        assertThat(loopElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement usageOidElement = (EnotElement) loopElement.getBody();
        assertThat(usageOidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(usageOidElement.getJsonPath()).isEqualTo("/body/2/body/body/body");
        assertThat(usageOidElement.isOptional()).isFalse();
        assertThat(usageOidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) usageOidElement.getBody()).isEqualTo("${usage}");
    }

    @Test
    void testParseKeyUsageExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-key-usage.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.15");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${key_usage_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement bitStringElement = (EnotElement) octetStringElement.getBody();
        assertThat(bitStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(bitStringElement.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(bitStringElement.isOptional()).isFalse();
        assertThat(bitStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "bit_string", Asn1Attribute.APPLY_PADDING, true));
        assertThat(bitStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement bitMapElement = (EnotElement) bitStringElement.getBody();
        assertThat(bitMapElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(bitMapElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(bitMapElement.isOptional()).isFalse();
        assertThat(bitMapElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "bit_map", SystemAttribute.BYTE_ORDER, "big_endian", SystemAttribute.BIT_ORDER, "msb_first"));
        assertThat(bitMapElement.getBody()).isInstanceOf(List.class);

        List<String> bitMapBody = (List<String>) bitMapElement.getBody();
        assertThat(bitMapBody).hasSize(9);
        assertThat(bitMapBody).containsExactly(
                "${digital_signature}",
                "${non_repudiation}",
                "${key_encipherment}",
                "${data_encipherment}",
                "${key_agreement}",
                "${key_cert_sign}",
                "${crl_sign}",
                "${encipher_only}",
                "${decipher_only}"
        );
    }

    @Test
    void testParseCertificatePolicyExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-certificate-policy.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.32");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${certificate_policy_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement outerSequenceElement = (EnotElement) octetStringElement.getBody();
        assertThat(outerSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(outerSequenceElement.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(outerSequenceElement.isOptional()).isFalse();
        assertThat(outerSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(outerSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement optionalSequenceElement = (EnotElement) outerSequenceElement.getBody();
        assertThat(optionalSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(optionalSequenceElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(optionalSequenceElement.isOptional()).isTrue();
        assertThat(optionalSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(optionalSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement loopElement = (EnotElement) optionalSequenceElement.getBody();
        assertThat(loopElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(loopElement.getJsonPath()).isEqualTo("/body/2/body/body/body");
        assertThat(loopElement.isOptional()).isFalse();
        assertThat(loopElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.ITEMS_NAME, "certificate_policy", SystemAttribute.KIND, "loop"));
        assertThat(loopElement.getBody()).isInstanceOf(List.class);

        List<?> loopBody = (List<?>) loopElement.getBody();
        assertThat(loopBody).hasSize(1);
        assertThat(loopBody.get(0)).isInstanceOf(EnotElement.class);

        EnotElement certificatePolicyElement = (EnotElement) loopBody.get(0);
        assertThat(certificatePolicyElement.getJsonPath()).isEqualTo("/body/2/body/body/body/body/0");
    }

    @Test
    void testParseAuthorityKeyIdentifierExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-authority-key-identifier.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.35");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${authority_key_identifier_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sha1Element = (EnotElement) octetStringElement.getBody();
        assertThat(sha1Element.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(sha1Element.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(sha1Element.isOptional()).isFalse();
        assertThat(sha1Element.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "sha1"));
        assertThat(sha1Element.getBody()).isInstanceOf(EnotElement.class);

        EnotElement hexToBinElement = (EnotElement) sha1Element.getBody();
        assertThat(hexToBinElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(hexToBinElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(hexToBinElement.isOptional()).isFalse();
        assertThat(hexToBinElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "hex_to_bin"));
        assertThat((String) hexToBinElement.getBody()).isEqualTo("${issuer_public_key_hex}");
    }

    @Test
    void testParseX509TBSValidityJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/x509-tbs-validity.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(4);

        EnotElement validFromGeneralizedCondition = (EnotElement) rootBody.get(0);
        assertThat(validFromGeneralizedCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(validFromGeneralizedCondition.getJsonPath()).isEqualTo("/body/0");
        assertThat(validFromGeneralizedCondition.isOptional()).isFalse();
        assertThat(validFromGeneralizedCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"));
        assertThat(validFromGeneralizedCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement validFromGeneralizedTime = (EnotElement) validFromGeneralizedCondition.getBody();
        assertThat(validFromGeneralizedTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(validFromGeneralizedTime.getJsonPath()).isEqualTo("/body/0/body");
        assertThat(validFromGeneralizedTime.isOptional()).isFalse();
        assertThat(validFromGeneralizedTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "generalized_time"));
        assertThat((String) validFromGeneralizedTime.getBody()).isEqualTo("${valid_from}");

        EnotElement validFromUtcCondition = (EnotElement) rootBody.get(1);
        assertThat(validFromUtcCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(validFromUtcCondition.getJsonPath()).isEqualTo("/body/1");
        assertThat(validFromUtcCondition.isOptional()).isFalse();
        assertThat(validFromUtcCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${valid_from}) < date_time('2050-01-01T00:00:00Z')"));
        assertThat(validFromUtcCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement validFromUtcTime = (EnotElement) validFromUtcCondition.getBody();
        assertThat(validFromUtcTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(validFromUtcTime.getJsonPath()).isEqualTo("/body/1/body");
        assertThat(validFromUtcTime.isOptional()).isFalse();
        assertThat(validFromUtcTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utc_time"));
        assertThat((String) validFromUtcTime.getBody()).isEqualTo("${valid_from}");

        EnotElement expiresOnGeneralizedCondition = (EnotElement) rootBody.get(2);
        assertThat(expiresOnGeneralizedCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(expiresOnGeneralizedCondition.getJsonPath()).isEqualTo("/body/2");
        assertThat(expiresOnGeneralizedCondition.isOptional()).isFalse();
        assertThat(expiresOnGeneralizedCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${expires_on}) >= date_time('2050-01-01T00:00:00Z')"));
        assertThat(expiresOnGeneralizedCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement expiresOnGeneralizedTime = (EnotElement) expiresOnGeneralizedCondition.getBody();
        assertThat(expiresOnGeneralizedTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(expiresOnGeneralizedTime.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(expiresOnGeneralizedTime.isOptional()).isFalse();
        assertThat(expiresOnGeneralizedTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "generalized_time"));
        assertThat((String) expiresOnGeneralizedTime.getBody()).isEqualTo("${expires_on}");

        EnotElement expiresOnUtcCondition = (EnotElement) rootBody.get(3);
        assertThat(expiresOnUtcCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(expiresOnUtcCondition.getJsonPath()).isEqualTo("/body/3");
        assertThat(expiresOnUtcCondition.isOptional()).isFalse();
        assertThat(expiresOnUtcCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"));
        assertThat(expiresOnUtcCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement expiresOnUtcTime = (EnotElement) expiresOnUtcCondition.getBody();
        assertThat(expiresOnUtcTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(expiresOnUtcTime.getJsonPath()).isEqualTo("/body/3/body");
        assertThat(expiresOnUtcTime.isOptional()).isFalse();
        assertThat(expiresOnUtcTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utc_time"));
        assertThat((String) expiresOnUtcTime.getBody()).isEqualTo("${expires_on}");
    }

    @Test
    void testParseCommonNameYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/subject-dn-common-name.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "set"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sequenceElement = (EnotElement) root.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.getJsonPath()).isEqualTo("/body");
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody()).isInstanceOf(List.class);

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(2);

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.4.3");

        EnotElement utf8StringElement = (EnotElement) sequenceBody.get(1);
        assertThat(utf8StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(utf8StringElement.getJsonPath()).isEqualTo("/body/body/1");
        assertThat(utf8StringElement.isOptional()).isFalse();
        assertThat(utf8StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utf8_string"));
        assertThat((String) utf8StringElement.getBody()).isEqualTo("${common_name}");
    }

    @Test
    void testParseOrganizationalUnitYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/subject-dn-organizational-unit.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "loop", SystemAttribute.ITEMS_NAME, "organizational_units"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement setElement = (EnotElement) root.getBody();
        assertThat(setElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(setElement.getJsonPath()).isEqualTo("/body");
        assertThat(setElement.isOptional()).isFalse();
        assertThat(setElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "set"));
        assertThat(setElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sequenceElement = (EnotElement) setElement.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.getJsonPath()).isEqualTo("/body/body");
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody()).isInstanceOf(List.class);

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(2);

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/body/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.4.11");

        EnotElement utf8StringElement = (EnotElement) sequenceBody.get(1);
        assertThat(utf8StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(utf8StringElement.getJsonPath()).isEqualTo("/body/body/body/1");
        assertThat(utf8StringElement.isOptional()).isFalse();
        assertThat(utf8StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utf8_string"));
        assertThat((String) utf8StringElement.getBody()).isEqualTo("${unit}");
    }

    @Test
    void testParseExtendedKeyUsageExtensionYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/extension-extended-key-usage.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.37");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${extended_key_usage_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement innerSequenceElement = (EnotElement) octetStringElement.getBody();
        assertThat(innerSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(innerSequenceElement.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(innerSequenceElement.isOptional()).isFalse();
        assertThat(innerSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(innerSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement loopElement = (EnotElement) innerSequenceElement.getBody();
        assertThat(loopElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(loopElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(loopElement.isOptional()).isFalse();
        assertThat(loopElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "loop", SystemAttribute.ITEMS_NAME, "extended_key_usage"));
        assertThat(loopElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement usageOidElement = (EnotElement) loopElement.getBody();
        assertThat(usageOidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(usageOidElement.getJsonPath()).isEqualTo("/body/2/body/body/body");
        assertThat(usageOidElement.isOptional()).isFalse();
        assertThat(usageOidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) usageOidElement.getBody()).isEqualTo("${usage}");
    }

    @Test
    void testParseKeyUsageExtensionYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/extension-key-usage.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.15");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${key_usage_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement bitStringElement = (EnotElement) octetStringElement.getBody();
        assertThat(bitStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(bitStringElement.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(bitStringElement.isOptional()).isFalse();
        assertThat(bitStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "bit_string", Asn1Attribute.APPLY_PADDING, true));
        assertThat(bitStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement bitMapElement = (EnotElement) bitStringElement.getBody();
        assertThat(bitMapElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(bitMapElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(bitMapElement.isOptional()).isFalse();
        assertThat(bitMapElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "bit_map", SystemAttribute.BYTE_ORDER, "big_endian", SystemAttribute.BIT_ORDER, "msb_first"));
        assertThat(bitMapElement.getBody()).isInstanceOf(List.class);

        List<String> bitMapBody = (List<String>) bitMapElement.getBody();
        assertThat(bitMapBody).hasSize(9);
        assertThat(bitMapBody).containsExactly(
                "${digital_signature}",
                "${non_repudiation}",
                "${key_encipherment}",
                "${data_encipherment}",
                "${key_agreement}",
                "${key_cert_sign}",
                "${crl_sign}",
                "${encipher_only}",
                "${decipher_only}"
        );
    }

    @Test
    void testParseCertificatePolicyExtensionYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/extension-certificate-policy.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.32");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${certificate_policy_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement outerSequenceElement = (EnotElement) octetStringElement.getBody();
        assertThat(outerSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(outerSequenceElement.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(outerSequenceElement.isOptional()).isFalse();
        assertThat(outerSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(outerSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement optionalSequenceElement = (EnotElement) outerSequenceElement.getBody();
        assertThat(optionalSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(optionalSequenceElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(optionalSequenceElement.isOptional()).isTrue();
        assertThat(optionalSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(optionalSequenceElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement loopElement = (EnotElement) optionalSequenceElement.getBody();
        assertThat(loopElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(loopElement.getJsonPath()).isEqualTo("/body/2/body/body/body");
        assertThat(loopElement.isOptional()).isFalse();
        assertThat(loopElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.ITEMS_NAME, "certificate_policy", SystemAttribute.KIND, "loop"));
        assertThat(loopElement.getBody()).isInstanceOf(List.class);

        List<?> loopBody = (List<?>) loopElement.getBody();
        assertThat(loopBody).hasSize(1);
        assertThat(loopBody.get(0)).isInstanceOf(EnotElement.class);

        EnotElement certificatePolicyElement = (EnotElement) loopBody.get(0);
        assertThat(certificatePolicyElement.getJsonPath()).isEqualTo("/body/2/body/body/body/body/0");
    }

    @Test
    void testParseAuthorityKeyIdentifierExtensionYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/extension-authority-key-identifier.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(3);

        EnotElement oidElement = (EnotElement) rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("2.5.29.35");

        EnotElement criticalElement = (EnotElement) rootBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${authority_key_identifier_critical}");

        EnotElement octetStringElement = (EnotElement) rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sha1Element = (EnotElement) octetStringElement.getBody();
        assertThat(sha1Element.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(sha1Element.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(sha1Element.isOptional()).isFalse();
        assertThat(sha1Element.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "sha1"));
        assertThat(sha1Element.getBody()).isInstanceOf(EnotElement.class);

        EnotElement hexToBinElement = (EnotElement) sha1Element.getBody();
        assertThat(hexToBinElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(hexToBinElement.getJsonPath()).isEqualTo("/body/2/body/body");
        assertThat(hexToBinElement.isOptional()).isFalse();
        assertThat(hexToBinElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "hex_to_bin"));
        assertThat((String) hexToBinElement.getBody()).isEqualTo("${issuer_public_key_hex}");
    }

    @Test
    void testParseX509TBSValidityYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/x509-tbs-validity.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        List<?> rootBody = (List<?>) root.getBody();
        assertThat(rootBody).hasSize(4);

        EnotElement validFromGeneralizedCondition = (EnotElement) rootBody.get(0);
        assertThat(validFromGeneralizedCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(validFromGeneralizedCondition.getJsonPath()).isEqualTo("/body/0");
        assertThat(validFromGeneralizedCondition.isOptional()).isFalse();
        assertThat(validFromGeneralizedCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${valid_from}) >= date_time('2050-01-01T00:00:00Z')"));
        assertThat(validFromGeneralizedCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement validFromGeneralizedTime = (EnotElement) validFromGeneralizedCondition.getBody();
        assertThat(validFromGeneralizedTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(validFromGeneralizedTime.getJsonPath()).isEqualTo("/body/0/body");
        assertThat(validFromGeneralizedTime.isOptional()).isFalse();
        assertThat(validFromGeneralizedTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "generalized_time"));
        assertThat((String) validFromGeneralizedTime.getBody()).isEqualTo("${valid_from}");

        EnotElement validFromUtcCondition = (EnotElement) rootBody.get(1);
        assertThat(validFromUtcCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(validFromUtcCondition.getJsonPath()).isEqualTo("/body/1");
        assertThat(validFromUtcCondition.isOptional()).isFalse();
        assertThat(validFromUtcCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${valid_from}) < date_time('2050-01-01T00:00:00Z')"));
        assertThat(validFromUtcCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement validFromUtcTime = (EnotElement) validFromUtcCondition.getBody();
        assertThat(validFromUtcTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(validFromUtcTime.getJsonPath()).isEqualTo("/body/1/body");
        assertThat(validFromUtcTime.isOptional()).isFalse();
        assertThat(validFromUtcTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utc_time"));
        assertThat((String) validFromUtcTime.getBody()).isEqualTo("${valid_from}");

        EnotElement expiresOnGeneralizedCondition = (EnotElement) rootBody.get(2);
        assertThat(expiresOnGeneralizedCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(expiresOnGeneralizedCondition.getJsonPath()).isEqualTo("/body/2");
        assertThat(expiresOnGeneralizedCondition.isOptional()).isFalse();
        assertThat(expiresOnGeneralizedCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${expires_on}) >= date_time('2050-01-01T00:00:00Z')"));
        assertThat(expiresOnGeneralizedCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement expiresOnGeneralizedTime = (EnotElement) expiresOnGeneralizedCondition.getBody();
        assertThat(expiresOnGeneralizedTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(expiresOnGeneralizedTime.getJsonPath()).isEqualTo("/body/2/body");
        assertThat(expiresOnGeneralizedTime.isOptional()).isFalse();
        assertThat(expiresOnGeneralizedTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "generalized_time"));
        assertThat((String) expiresOnGeneralizedTime.getBody()).isEqualTo("${expires_on}");

        EnotElement expiresOnUtcCondition = (EnotElement) rootBody.get(3);
        assertThat(expiresOnUtcCondition.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(expiresOnUtcCondition.getJsonPath()).isEqualTo("/body/3");
        assertThat(expiresOnUtcCondition.isOptional()).isFalse();
        assertThat(expiresOnUtcCondition.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "condition", SystemAttribute.EXPRESSION, "date_time(${expires_on}) < date_time('2050-01-01T00:00:00Z')"));
        assertThat(expiresOnUtcCondition.getBody()).isInstanceOf(EnotElement.class);

        EnotElement expiresOnUtcTime = (EnotElement) expiresOnUtcCondition.getBody();
        assertThat(expiresOnUtcTime.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(expiresOnUtcTime.getJsonPath()).isEqualTo("/body/3/body");
        assertThat(expiresOnUtcTime.isOptional()).isFalse();
        assertThat(expiresOnUtcTime.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utc_time"));
        assertThat((String) expiresOnUtcTime.getBody()).isEqualTo("${expires_on}");
    }

    @Test
    void testParseAuthorityInfoAccessExtensionYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/extension-authority-info-access.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "group", SystemAttribute.GROUP_NAME, "authority_info_access"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement sequenceElement = (EnotElement) root.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.getJsonPath()).isEqualTo("/body");
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody()).isInstanceOf(List.class);

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(3);

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getJsonPath()).isEqualTo("/body/body/0");
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) oidElement.getBody()).isEqualTo("1.3.6.1.5.5.7.1.1");

        EnotElement criticalElement = (EnotElement) sequenceBody.get(1);
        assertThat(criticalElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(criticalElement.getJsonPath()).isEqualTo("/body/body/1");
        assertThat(criticalElement.isOptional()).isTrue();
        assertThat(criticalElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat((String) criticalElement.getBody()).isEqualTo("${critical}");

        EnotElement octetStringElement = (EnotElement) sequenceBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/body/2");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement accessDescriptionsSequence = (EnotElement) octetStringElement.getBody();
        assertThat(accessDescriptionsSequence.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(accessDescriptionsSequence.getJsonPath()).isEqualTo("/body/body/2/body");
        assertThat(accessDescriptionsSequence.isOptional()).isFalse();
        assertThat(accessDescriptionsSequence.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(accessDescriptionsSequence.getBody()).isInstanceOf(List.class);

        List<?> accessDescriptions = (List<?>) accessDescriptionsSequence.getBody();
        assertThat(accessDescriptions).hasSize(2);

        // [0] OCSP access description: OID + explicit [6] IA5String with "${system.issuer_ocsp_url}"
        EnotElement ocspSequenceElement = (EnotElement) accessDescriptions.get(0);
        assertThat(ocspSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(ocspSequenceElement.getJsonPath()).isEqualTo("/body/body/2/body/body/0");
        assertThat(ocspSequenceElement.isOptional()).isFalse();
        assertThat(ocspSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(ocspSequenceElement.getBody()).isInstanceOf(List.class);

        List<?> ocspSequenceBody = (List<?>) ocspSequenceElement.getBody();
        assertThat(ocspSequenceBody).hasSize(2);

        EnotElement ocspOidElement = (EnotElement) ocspSequenceBody.get(0);
        assertThat(ocspOidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(ocspOidElement.getJsonPath()).isEqualTo("/body/body/2/body/body/0/body/0");
        assertThat(ocspOidElement.isOptional()).isFalse();
        assertThat(ocspOidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) ocspOidElement.getBody()).isEqualTo("1.3.6.1.5.5.7.48.1");

        EnotElement ocspTaggedObjectElement = (EnotElement) ocspSequenceBody.get(1);
        assertThat(ocspTaggedObjectElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(ocspTaggedObjectElement.getJsonPath()).isEqualTo("/body/body/2/body/body/0/body/1");
        assertThat(ocspTaggedObjectElement.isOptional()).isFalse();
        assertThat(ocspTaggedObjectElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "tagged_object", Asn1Attribute.EXPLICIT, 6));
        assertThat(ocspTaggedObjectElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement ocspIa5StringElement = (EnotElement) ocspTaggedObjectElement.getBody();
        assertThat(ocspIa5StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(ocspIa5StringElement.getJsonPath()).isEqualTo("/body/body/2/body/body/0/body/1/body");
        assertThat(ocspIa5StringElement.isOptional()).isFalse();
        assertThat(ocspIa5StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "ia5_string"));
        assertThat((String) ocspIa5StringElement.getBody()).isEqualTo("${system.issuer_ocsp_url}");

        // [1] CA Issuers access description: OID + explicit [6] IA5String with "${system.issuer_download_url}"
        EnotElement caIssuersSequenceElement = (EnotElement) accessDescriptions.get(1);
        assertThat(caIssuersSequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(caIssuersSequenceElement.getJsonPath()).isEqualTo("/body/body/2/body/body/1");
        assertThat(caIssuersSequenceElement.isOptional()).isFalse();
        assertThat(caIssuersSequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(caIssuersSequenceElement.getBody()).isInstanceOf(List.class);

        List<?> caIssuersSequenceBody = (List<?>) caIssuersSequenceElement.getBody();
        assertThat(caIssuersSequenceBody).hasSize(2);

        EnotElement caIssuersOidElement = (EnotElement) caIssuersSequenceBody.get(0);
        assertThat(caIssuersOidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(caIssuersOidElement.getJsonPath()).isEqualTo("/body/body/2/body/body/1/body/0");
        assertThat(caIssuersOidElement.isOptional()).isFalse();
        assertThat(caIssuersOidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat((String) caIssuersOidElement.getBody()).isEqualTo("1.3.6.1.5.5.7.48.2");

        EnotElement caIssuersTaggedObjectElement = (EnotElement) caIssuersSequenceBody.get(1);
        assertThat(caIssuersTaggedObjectElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(caIssuersTaggedObjectElement.getJsonPath()).isEqualTo("/body/body/2/body/body/1/body/1");
        assertThat(caIssuersTaggedObjectElement.isOptional()).isFalse();
        assertThat(caIssuersTaggedObjectElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "tagged_object", Asn1Attribute.EXPLICIT, 6));
        assertThat(caIssuersTaggedObjectElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement caIssuersIa5StringElement = (EnotElement) caIssuersTaggedObjectElement.getBody();
        assertThat(caIssuersIa5StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(caIssuersIa5StringElement.getJsonPath()).isEqualTo("/body/body/2/body/body/1/body/1/body");
        assertThat(caIssuersIa5StringElement.isOptional()).isFalse();
        assertThat(caIssuersIa5StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "ia5_string"));
        assertThat((String) caIssuersIa5StringElement.getBody()).isEqualTo("${system.issuer_download_url}");
    }

    @Test
    void testParseSanIpAddressExtensionYamlSuccess() throws Exception {

        String path = "yaml/asn1/rfc/extension-san-ip-address.yaml";
        String yaml = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(yaml, enotContext);

        assertThat(actual).hasSize(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(root.getJsonPath()).isEqualTo("");
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "loop", SystemAttribute.ITEMS_NAME, "ip_address"));
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);

        EnotElement taggedObjectElement = (EnotElement) root.getBody();
        assertThat(taggedObjectElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(taggedObjectElement.getJsonPath()).isEqualTo("/body");
        assertThat(taggedObjectElement.isOptional()).isFalse();
        assertThat(taggedObjectElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "tagged_object", Asn1Attribute.IMPLICIT, 7));
        assertThat(taggedObjectElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement octetStringElement = (EnotElement) taggedObjectElement.getBody();
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getJsonPath()).isEqualTo("/body/body");
        assertThat(octetStringElement.isOptional()).isFalse();
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));
        assertThat(octetStringElement.getBody()).isInstanceOf(EnotElement.class);

        EnotElement ipAddressToBytesElement = (EnotElement) octetStringElement.getBody();
        assertThat(ipAddressToBytesElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(ipAddressToBytesElement.getJsonPath()).isEqualTo("/body/body/body");
        assertThat(ipAddressToBytesElement.isOptional()).isFalse();
        assertThat(ipAddressToBytesElement.getAttributes()).isEqualTo(Map.of(SystemAttribute.KIND, "ip_address_to_bytes"));
        assertThat((String) ipAddressToBytesElement.getBody()).isEqualTo("${address}");
    }
}
