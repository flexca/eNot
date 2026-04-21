package com.github.flexca.enot.bertlv;

import com.github.flexca.enot.core.Enot;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.exception.EnotSerializationException;
import com.github.flexca.enot.core.parser.EnotJsonError;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.context.SerializationContext;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnotBerTlvFailureCasesTest {

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

    private static boolean hasError(String detailsFragment, List<EnotJsonError> errors) {
        return errors.stream().anyMatch(e -> e.getDetails().contains(detailsFragment));
    }

    // -----------------------------------------------------------------------
    // Template validation failures  →  EnotParsingException at parse time
    // -----------------------------------------------------------------------

    @Test
    void testMissingTagThrowsParsingException() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/template/missing-tag.json");

        EnotParsingException ex = assertThrows(EnotParsingException.class,
                () -> enot.serialize(json, ctx(Map.of())));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).isNotEmpty();
        assertThat(hasError("missing tag attribute", errors)).isTrue();
    }

    @Test
    void testInvalidHexTagThrowsParsingException() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/template/invalid-hex-tag.json");

        EnotParsingException ex = assertThrows(EnotParsingException.class,
                () -> enot.serialize(json, ctx(Map.of())));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).hasSize(1);
        assertThat(hasError("must be hex string", errors)).isTrue();
    }

    @Test
    void testMalformedTagThrowsParsingException() throws Exception {

        // tag "1F" is a long-form tag that requires a continuation byte — structurally invalid
        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/template/malformed-tag.json");

        EnotParsingException ex = assertThrows(EnotParsingException.class,
                () -> enot.serialize(json, ctx(Map.of())));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).hasSize(1);
        assertThat(hasError("formed incorrectly", errors)).isTrue();
    }

    @Test
    void testTagTooLongThrowsParsingException() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/template/tag-too-long.json");

        EnotParsingException ex = assertThrows(EnotParsingException.class,
                () -> enot.serialize(json, ctx(Map.of())));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).isNotEmpty();
        assertThat(hasError("must not exceed 4 bytes", errors)).isTrue();
    }

    @Test
    void testMinLengthGreaterThanMaxLengthThrowsParsingException() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/template/min-greater-than-max.json");

        EnotParsingException ex = assertThrows(EnotParsingException.class,
                () -> enot.serialize(json, ctx(Map.of())));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).hasSize(1);
        assertThat(hasError("min_length", errors)).isTrue();
        assertThat(hasError("max_length", errors)).isTrue();
    }

    @Test
    void testIndefiniteFormNotBooleanThrowsParsingException() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/template/indefinite-form-not-boolean.json");

        EnotParsingException ex = assertThrows(EnotParsingException.class,
                () -> enot.serialize(json, ctx(Map.of())));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).isNotEmpty();
        assertThat(hasError("indefinite_form", errors)).isTrue();
        assertThat(hasError("must be boolean", errors)).isTrue();
    }

    // -----------------------------------------------------------------------
    // Data serialization failures  →  EnotSerializationException
    // -----------------------------------------------------------------------

    @Test
    void testMinLengthViolationThrowsSerializationException() throws Exception {

        // value "AABB" = 2 bytes, min_length = 4  →  violation
        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/data/min-length-violation.json");

        EnotSerializationException ex = assertThrows(EnotSerializationException.class,
                () -> enot.serialize(json, ctx(Map.of("value", "AABB"))));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).hasSize(1);
        assertThat(hasError("is less than required min_length", errors)).isTrue();
    }

    @Test
    void testMaxLengthViolationThrowsSerializationException() throws Exception {

        // value "AABB" = 2 bytes, max_length = 1  →  violation
        String json = ResourceReaderTestUtils.readResourceFileAsString("failure/data/max-length-violation.json");

        EnotSerializationException ex = assertThrows(EnotSerializationException.class,
                () -> enot.serialize(json, ctx(Map.of("value", "AABB"))));

        List<EnotJsonError> errors = ex.getJsonErrors();
        assertThat(errors).hasSize(1);
        assertThat(hasError("exceeds max_length", errors)).isTrue();
    }
}
