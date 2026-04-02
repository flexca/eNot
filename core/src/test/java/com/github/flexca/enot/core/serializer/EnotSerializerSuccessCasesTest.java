package com.github.flexca.enot.core.serializer;

import com.github.flexca.enot.core.parser.EnotParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotSerializerSuccessCasesTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private EnotRegistry enotRegistry;

    private EnotParser enotParser;

    private EnotSerializer enotSerializer;

    @BeforeEach
    void init() {
        enotRegistry = new EnotRegistry(new SystemTypeSpecification(), new Asn1TypeSpecification());
        enotParser = new EnotParser(enotRegistry, objectMapper);
        enotSerializer = new EnotSerializer(enotRegistry, enotParser);
    }

    @Test
    void testSerializeCommonNameSuccess() throws Exception {

        String path = "json/asn1/rfc/subject-dn-common-name.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        Map<String, Object> params = Map.of("common_name", "example");
        List<byte[]> actual = enotSerializer.serialize(json, params);

        assertThat(actual).isNotNull();
    }
}
