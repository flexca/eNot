package io.github.flexca.enot.bertlv;

import io.github.flexca.enot.core.Enot;
import io.github.flexca.enot.core.registry.EnotRegistry;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import io.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnotBerTlvSuccessCasesTest {

    private ObjectMapper jsonObjectMapper;
    private ObjectMapper yamlObjectMapper;
    private EnotRegistry enotRegistry;
    private Enot enot;

    @BeforeEach
    void init() {

        jsonObjectMapper = new ObjectMapper();
        yamlObjectMapper = new ObjectMapper(new YAMLFactory());
        enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification())
                .withTypeSpecifications(new BerTlvEnotTypeSpecification())
                .build();

        enot = new Enot.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withRegistry(enotRegistry)
                .build();
    }

    private SerializationContext ctx(Map<String, Object> params) {
        return new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams(params)
                .build();
    }

    // -----------------------------------------------------------------------
    // simple-case: constructed 0x73 with two leaf children (hex_to_bin body)
    // expected: 73 14 06 08 <8 bytes> 04 08 <8 bytes>
    // -----------------------------------------------------------------------

    @Test
    void testSimpleCase() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("success/simple-case.json");

        List<byte[]> actual = enot.serialize(json, ctx(Map.of(
                "value1", "1122334455667788",
                "value2", "8877665544332211")));

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).hasSize(22);
        assertThat(HexFormat.of().formatHex(actual.get(0)))
                .isEqualTo("73140608112233445566778804081122334455667788");
    }

    // -----------------------------------------------------------------------
    // leaf element: single primitive TLV  →  04 02 AA BB
    // -----------------------------------------------------------------------

    @Test
    void testLeafElementBytes() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("success/leaf-element.json");

        List<byte[]> actual = enot.serialize(json, ctx(Map.of("value", "AABB")));

        assertThat(actual).hasSize(1);
        assertThat(HexFormat.of().formatHex(actual.get(0))).isEqualTo("0402aabb");
    }

    // -----------------------------------------------------------------------
    // node element: constructed 0x73 with single child leaf 0x04
    // expected: 73 04 04 02 AA BB
    // -----------------------------------------------------------------------

    @Test
    void testNodeElementBytes() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("success/node-element.json");

        List<byte[]> actual = enot.serialize(json, ctx(Map.of("value", "AABB")));

        assertThat(actual).hasSize(1);
        assertThat(HexFormat.of().formatHex(actual.get(0))).isEqualTo("7304 0402aabb".replace(" ", ""));
    }

    // -----------------------------------------------------------------------
    // multi-byte tag: 2-byte tag 9F20  →  9F 20 02 AA BB
    // -----------------------------------------------------------------------

    @Test
    void testMultiByteTagBytes() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("success/multi-byte-tag.json");

        List<byte[]> actual = enot.serialize(json, ctx(Map.of("value", "AABB")));

        assertThat(actual).hasSize(1);
        assertThat(HexFormat.of().formatHex(actual.get(0))).isEqualTo("9f200 2aabb".replace(" ", ""));
    }

    // -----------------------------------------------------------------------
    // length constraints: value within [1, 10] should pass without error
    // -----------------------------------------------------------------------

    @Test
    void testLengthConstraintsSatisfied() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("success/with-length-constraints.json");

        List<byte[]> actual = enot.serialize(json, ctx(Map.of("value", "AABB")));

        assertThat(actual).hasSize(1);
        assertThat(HexFormat.of().formatHex(actual.get(0))).isEqualTo("0402aabb");
    }

    // -----------------------------------------------------------------------
    // indefinite form: 04 80 AA BB 00 00
    // -----------------------------------------------------------------------

    @Test
    void testIndefiniteFormBytes() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("success/indefinite-form.json");

        List<byte[]> actual = enot.serialize(json, ctx(Map.of("value", "AABB")));

        assertThat(actual).hasSize(1);
        assertThat(HexFormat.of().formatHex(actual.get(0))).isEqualTo("0480aabb0000");
    }
}
