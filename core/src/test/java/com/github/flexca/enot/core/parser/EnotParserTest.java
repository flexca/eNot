package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.struct.EnotElement;
import com.github.flexca.enot.core.system.SystemTypeSpecification;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class EnotParserTest {

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

    }

    @Test
    void testParseExtendedKeyUsageExtensionJsonSuccess() throws Exception {

        String path = "json/asn1/rfc/extension-extended-key-usage.json";
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
