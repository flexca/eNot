package com.github.flexca.enot.core.extractor;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.serializer.context.ContextArray;
import com.github.flexca.enot.core.serializer.context.ContextMap;
import com.github.flexca.enot.core.serializer.context.ContextPrimitive;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import com.github.flexca.enot.core.testutil.TestResourcesReferenceResolver;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleParamsExtractorTest {

    private EnotContext enotContext;
    private EnotParser enotParser;
    private ExampleParamsExtractor extractor;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .withElementReferenceResolver(new TestResourcesReferenceResolver())
                .build();
        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        enotParser = new EnotParser(new ObjectMapper(), new ObjectMapper(new YAMLFactory()));
        enotContext = new EnotContext(enotRegistry, enotParser, new EnotSerializer(enotParser),
                conditionExpressionParser, new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser));
        extractor = new ExampleParamsExtractor(enotContext);
    }

    @Test
    void testExtractParamsFromSimpleElement() throws Exception {
        List<EnotElement> elements = parse("json/asn1/rfc/subject-dn-common-name.json");

        ContextMap result = extractor.extractExampleParams(elements);

        assertThat(result.getItems()).containsOnlyKeys("common_name");
        assertThatPrimitive(result.get("common_name"));
    }

    @Test
    void testExtractParamsFromConditionExpressions() throws Exception {
        List<EnotElement> elements = parse("json/asn1/rfc/x509-tbs-validity.json");

        ContextMap result = extractor.extractExampleParams(elements);

        // ${valid_from} and ${expires_on} appear in both the expression attribute and the element body —
        // both are extracted into the same flat scope, so keys are deduplicated
        assertThat(result.getItems()).containsOnlyKeys("valid_from", "expires_on");
        assertThatPrimitive(result.get("valid_from"));
        assertThatPrimitive(result.get("expires_on"));
    }

    @Test
    void testExtractParamsFromKeyUsage() throws Exception {
        List<EnotElement> elements = parse("json/asn1/rfc/extension-key-usage.json");

        ContextMap result = extractor.extractExampleParams(elements);

        assertThat(result.getItems()).containsOnlyKeys(
                "key_usage_critical",
                "digital_signature", "non_repudiation", "key_encipherment",
                "data_encipherment", "key_agreement", "key_cert_sign",
                "crl_sign", "encipher_only", "decipher_only");
        result.getItems().values().forEach(this::assertThatPrimitive);
    }

    @Test
    void testExtractParamsFromSanWithGroupAndLoopScope() throws Exception {
        // extension-san.json contains a GROUP("san") wrapping a LOOP("dns_name") resolved via reference.
        // Expected structure: { san_critical: "...", san: { dns_name: [{ value: "..." }] } }
        List<EnotElement> elements = parse("json/asn1/rfc/san/extension-san.json");

        ContextMap result = extractor.extractExampleParams(elements);

        assertThat(result.getItems()).containsOnlyKeys("san_critical", "san");
        assertThatPrimitive(result.get("san_critical"));

        // MAP_SCOPE("san") from GROUP element
        assertThat(result.get("san")).isInstanceOf(ContextMap.class);
        ContextMap sanMap = (ContextMap) result.get("san");
        assertThat(sanMap.getItems()).containsOnlyKeys("dns_name");

        // ARRAY_SCOPE("dns_name") from LOOP element
        assertThat(sanMap.get("dns_name")).isInstanceOf(ContextArray.class);
        ContextArray dnsArray = (ContextArray) sanMap.get("dns_name");
        assertThat(dnsArray.getItems()).hasSize(1);

        // single example item map with the loop body placeholder
        assertThat(dnsArray.getItems().get(0)).isInstanceOf(ContextMap.class);
        ContextMap dnsItem = (ContextMap) dnsArray.getItems().get(0);
        assertThat(dnsItem.getItems()).containsOnlyKeys("value");
        assertThatPrimitive(dnsItem.get("value"));
    }

    @Test
    void testExtractParamsFromCertificatePolicyWithNestedLoops() throws Exception {
        List<EnotElement> elements = parse("json/asn1/rfc/extension-certificate-policy.json");

        ContextMap result = extractor.extractExampleParams(elements);

        // flat top-level placeholder (dot in name is not treated as a path separator)
        assertThat(result.getItems()).containsKey("certificate_policy.critical");
        assertThatPrimitive(result.get("certificate_policy.critical"));

        // outer LOOP: certificate_policy → [{ policy_oid, cps_qualifiers, unotice_qualifiers }]
        assertThat(result.get("certificate_policy")).isInstanceOf(ContextArray.class);
        ContextArray certPolicyArray = (ContextArray) result.get("certificate_policy");
        assertThat(certPolicyArray.getItems()).hasSize(1);
        ContextMap certPolicyItem = (ContextMap) certPolicyArray.getItems().get(0);
        assertThat(certPolicyItem.getItems()).containsKey("policy_oid");
        assertThatPrimitive(certPolicyItem.get("policy_oid"));

        // nested LOOP: cps_qualifiers → [{ cps }]
        assertThat(certPolicyItem.get("cps_qualifiers")).isInstanceOf(ContextArray.class);
        ContextArray cpsArray = (ContextArray) certPolicyItem.get("cps_qualifiers");
        assertThat(cpsArray.getItems()).hasSize(1);
        ContextMap cpsItem = (ContextMap) cpsArray.getItems().get(0);
        assertThat(cpsItem.getItems()).containsOnlyKeys("cps");
        assertThatPrimitive(cpsItem.get("cps"));

        // nested LOOP: unotice_qualifiers → [{ organization, explicit_text, notice_numbers }]
        assertThat(certPolicyItem.get("unotice_qualifiers")).isInstanceOf(ContextArray.class);
        ContextArray unoticeArray = (ContextArray) certPolicyItem.get("unotice_qualifiers");
        assertThat(unoticeArray.getItems()).hasSize(1);
        ContextMap unoticeItem = (ContextMap) unoticeArray.getItems().get(0);
        assertThat(unoticeItem.getItems()).containsKey("organization");
        assertThat(unoticeItem.getItems()).containsKey("explicit_text");
        assertThatPrimitive(unoticeItem.get("organization"));
        assertThatPrimitive(unoticeItem.get("explicit_text"));

        // deeply nested LOOP: notice_numbers → [{ number }]
        assertThat(unoticeItem.get("notice_numbers")).isInstanceOf(ContextArray.class);
        ContextArray noticeNumbersArray = (ContextArray) unoticeItem.get("notice_numbers");
        assertThat(noticeNumbersArray.getItems()).hasSize(1);
        ContextMap noticeNumberItem = (ContextMap) noticeNumbersArray.getItems().get(0);
        assertThat(noticeNumberItem.getItems()).containsOnlyKeys("number");
        assertThatPrimitive(noticeNumberItem.get("number"));
    }

    private List<EnotElement> parse(String resourcePath) throws Exception {
        String json = ResourceReaderTestUtils.readResourceFileAsString(resourcePath);
        return enotParser.parse(json, enotContext);
    }

    private void assertThatPrimitive(Object node) {
        assertThat(node).isInstanceOf(ContextPrimitive.class);
        assertThat(((ContextPrimitive) node).getValue()).isEqualTo("replace with your value");
    }
}
