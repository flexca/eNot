package io.github.flexca.enot.core;

import io.github.flexca.enot.core.element.EnotElement;
import io.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import io.github.flexca.enot.core.testutil.TestResourcesReferenceResolver;
import io.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnotTest {

    private static final String COMMON_NAME_JSON = "json/asn1/rfc/subject-dn-common-name.json";
    private static final String COMMON_NAME_OID  = "2.5.4.3";
    private static final String COMMON_NAME_VALUE = "Alice";

    private Enot enot;
    private ObjectMapper jsonObjectMapper;
    private ObjectMapper yamlObjectMapper;

    private String commonNameJson;

    @BeforeEach
    void init() throws Exception {
        jsonObjectMapper = new ObjectMapper();
        yamlObjectMapper = new ObjectMapper(new YAMLFactory());
        EnotRegistry registry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .withElementReferenceResolver(new TestResourcesReferenceResolver())
                .build();
        enot = new Enot.Builder()
                .withRegistry(registry)
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .build();
        commonNameJson = ResourceReaderTestUtils.readResourceFileAsString(COMMON_NAME_JSON);
    }

    // -----------------------------------------------------------------------
    // parse
    // -----------------------------------------------------------------------

    @Test
    void testParseReturnsElements() throws Exception {
        List<EnotElement> elements = enot.parse(commonNameJson);

        assertThat(elements).hasSize(1);
        EnotElement root = elements.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        // root is a SET containing a SEQUENCE
        assertThat(root.getBody()).isInstanceOf(EnotElement.class);
    }

    // -----------------------------------------------------------------------
    // serialize — three overloads produce identical output
    // -----------------------------------------------------------------------

    @Test
    void testSerializeFromJson() throws Exception {
        SerializationContext ctx = ctx(Map.of("common_name", COMMON_NAME_VALUE));

        List<byte[]> result = enot.serialize(commonNameJson, ctx);

        assertThat(result).hasSize(1);
        assertCommonNameDer(result.get(0), COMMON_NAME_VALUE);
    }

    @Test
    void testSerializeFromParsedElement() throws Exception {
        List<EnotElement> elements = enot.parse(commonNameJson);
        SerializationContext ctx = ctx(Map.of("common_name", COMMON_NAME_VALUE));

        List<byte[]> result = enot.serialize(elements.get(0), ctx);

        assertThat(result).hasSize(1);
        assertCommonNameDer(result.get(0), COMMON_NAME_VALUE);
    }

    @Test
    void testSerializeFromParsedList() throws Exception {
        List<EnotElement> elements = enot.parse(commonNameJson);
        SerializationContext ctx = ctx(Map.of("common_name", COMMON_NAME_VALUE));

        List<byte[]> result = enot.serialize(elements, ctx);

        assertThat(result).hasSize(1);
        assertCommonNameDer(result.get(0), COMMON_NAME_VALUE);
    }

    @Test
    void testAllSerializeOverloadsProduceSameOutput() throws Exception {
        SerializationContext ctx1 = ctx(Map.of("common_name", COMMON_NAME_VALUE));
        SerializationContext ctx2 = ctx(Map.of("common_name", COMMON_NAME_VALUE));
        SerializationContext ctx3 = ctx(Map.of("common_name", COMMON_NAME_VALUE));
        List<EnotElement> elements = enot.parse(commonNameJson);

        byte[] fromJson    = enot.serialize(commonNameJson, ctx1).get(0);
        byte[] fromElement = enot.serialize(elements.get(0), ctx2).get(0);
        byte[] fromList    = enot.serialize(elements, ctx3).get(0);

        assertThat(fromJson).isEqualTo(fromElement);
        assertThat(fromJson).isEqualTo(fromList);
    }

    // -----------------------------------------------------------------------
    // getParamsExample — three overloads
    // -----------------------------------------------------------------------

    @Test
    void testGetParamsExampleFromJson() throws Exception {
        Map<String, Object> result = enot.getParamsExample(commonNameJson);

        assertThat(result).containsOnlyKeys("common_name");
        assertThat(result.get("common_name")).isEqualTo("replace with your value");
    }

    @Test
    void testGetParamsExampleFromElement() throws Exception {
        List<EnotElement> elements = enot.parse(commonNameJson);

        Map<String, Object> result = enot.getParamsExample(elements.get(0));

        assertThat(result).containsOnlyKeys("common_name");
        assertThat(result.get("common_name")).isEqualTo("replace with your value");
    }

    @Test
    void testGetParamsExampleFromList() throws Exception {
        List<EnotElement> elements = enot.parse(commonNameJson);

        Map<String, Object> result = enot.getParamsExample(elements);

        assertThat(result).containsOnlyKeys("common_name");
        assertThat(result.get("common_name")).isEqualTo("replace with your value");
    }

    @Test
    void testGetParamsExampleJsonIsValidJson() throws Exception {
        String json = enot.getParamsExampleJson(commonNameJson);

        assertThat(json).isNotBlank();
        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = jsonObjectMapper.readValue(json, Map.class);
        assertThat(parsed).containsOnlyKeys("common_name");
        assertThat(parsed.get("common_name")).isEqualTo("replace with your value");
    }

    // -----------------------------------------------------------------------
    // Builder failures
    // -----------------------------------------------------------------------

    @Test
    void testBuilderFailsWhenRegistryNotSet() {
        EnotInvalidConfigurationException ex = assertThrows(EnotInvalidConfigurationException.class, () ->
                new Enot.Builder()
                        .withJsonObjectMapper(jsonObjectMapper)
                        .build());
        assertThat(ex.getMessage()).contains("enotRegistry must be set");
    }

    @Test
    void testBuilderFailsWhenBothMappersNotSet() {
        EnotRegistry registry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .build();
        EnotInvalidConfigurationException ex = assertThrows(EnotInvalidConfigurationException.class, () ->
                new Enot.Builder()
                        .withRegistry(registry)
                        .build());
        assertThat(ex.getMessage()).contains("at least one of jsonObjectMapper or yamlObjectMapper must be set");
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private SerializationContext ctx(Map<String, Object> params) {
        return new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
    }

    private void assertCommonNameDer(byte[] der, String expectedValue) throws Exception {
        // outer SET
        ASN1Set set = (ASN1Set) ASN1Primitive.fromByteArray(der);
        assertThat(set.size()).isEqualTo(1);
        // inner SEQUENCE { OID, UTF8String }
        ASN1Sequence seq = (ASN1Sequence) set.getObjectAt(0);
        assertThat(seq.size()).isEqualTo(2);
        assertThat(((ASN1ObjectIdentifier) seq.getObjectAt(0)).getId()).isEqualTo(COMMON_NAME_OID);
        assertThat(((DERUTF8String) seq.getObjectAt(1)).getString()).isEqualTo(expectedValue);
    }
}
