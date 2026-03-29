package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.element.attribute.EnotAttribute;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.system.SystemTypeSpecification;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
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

        assertThat(actual.size()).isEqualTo(1);

        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.isOptional()).isFalse();
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "set"));
        assertThat(root.getBody() instanceof EnotElement).isTrue();

        EnotElement sequenceElement = (EnotElement) root.getBody();
        assertThat(sequenceElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(sequenceElement.isOptional()).isFalse();
        assertThat(sequenceElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(sequenceElement.getBody() instanceof List<?>).isTrue();

        List<?> sequenceBody = (List<?>) sequenceElement.getBody();
        assertThat(sequenceBody).hasSize(2);
        assertThat(sequenceBody.get(0) instanceof EnotElement);
        assertThat(sequenceBody.get(1) instanceof EnotElement);

        EnotElement oidElement = (EnotElement) sequenceBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.isOptional()).isFalse();
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat(oidElement.getBody() instanceof String);
        assertThat(oidElement.getBody()).asString().isEqualTo("2.5.4.3");

        EnotElement utf8StringElement = (EnotElement) sequenceBody.get(1);
        assertThat(utf8StringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(utf8StringElement.isOptional()).isFalse();
        assertThat(utf8StringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "utf8_string"));
        assertThat(utf8StringElement.getBody() instanceof String);
        assertThat(utf8StringElement.getBody()).asString().isEqualTo("${common_name}");
    }

    @Test
    void testParseOrganizationalUnitJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/subject-dn-organizational-unit.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual.size()).isEqualTo(1);

    }

    @Test
    void testParseExtendedKeyUsageExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-extended-key-usage.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual.size()).isEqualTo(1);
    }

    @Test
    void testParseKeyUsageExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-key-usage.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual.size()).isEqualTo(1);

    }

    @Test
    void testParseCertificatePolicyExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-certificate-policy.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        List<EnotElement> actual = enotParser.parse(json);

        assertThat(actual.size()).isEqualTo(1);

    }

}
