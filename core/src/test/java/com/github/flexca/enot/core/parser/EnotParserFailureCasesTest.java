package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.system.SystemTypeSpecification;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnotParserFailureCasesTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private EnotRegistry enotRegistry;

    private EnotParser enotParser;

    @BeforeEach
    void init() {
        enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecification(new SystemTypeSpecification())
                .withTypeSpecification(new Asn1TypeSpecification())
                .build();
        enotParser = new EnotParser(enotRegistry, objectMapper);
    }

    @Test
    void testNullInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse(null);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("blank JSON input provided");
    }

    @Test
    void testEmptyInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("");
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("blank JSON input provided");
    }

    @Test
    void testBlankInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("    ");
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("blank JSON input provided");
    }

    @Test
    void testNotJsonInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("not json");
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
    }

    @Test
    void testMalformedJsonInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("{\"type\": \"system\"");
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
    }

    @Test
    void tesJsonPrimitiveInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("\"primitive\"");
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("eNot expecting object or array as root JSON node");
    }

    @Test
    void tesJsonEmptyObjectInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("{}");
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("required eNot element field type");
    }

    @Test
    void tesJsonEmptyArrayInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("[]");
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("no elements found");
    }

    @Test
    void testInvalidElementTypeCases() throws Exception {

        String path = "json/asn1/failures/invalid-type-cases.json";
        String json = ResourceReaderTestUtils.readResourceFileAsString(path);

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse(json);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
    }
}
