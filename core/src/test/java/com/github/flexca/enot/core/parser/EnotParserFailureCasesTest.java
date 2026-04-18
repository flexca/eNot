package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.exception.EnotParsingException;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.exc.UnexpectedEndOfInputException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnotParserFailureCasesTest {

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    private EnotContext enotContext;

    private EnotParser enotParser;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecification(new SystemTypeSpecification())
                .withTypeSpecification(new Asn1TypeSpecification())
                .build();
        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        enotParser = new EnotParser(jsonObjectMapper, yamlObjectMapper);
        enotContext = new EnotContext(enotRegistry, enotParser, new EnotSerializer(enotParser),
                conditionExpressionParser, new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser));
    }

    @Test
    void testNullInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse(null, enotContext);
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
            enotParser.parse("", enotContext);
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
            enotParser.parse("    ", enotContext);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("blank JSON input provided");
    }

    @Test
    void testNotJsonInput() throws Exception {

        // "not json" does not start with { or [ so it is treated as YAML.
        // YAML parses it as a plain scalar string — valid YAML, but not a valid eNot root node.
        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("not json", enotContext);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("eNot expecting object or array as root JSON node");
    }

    @Test
    void testMalformedJsonInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("{\"type\": \"system\"", enotContext);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(parsingException.getCause().getClass()).isEqualTo(UnexpectedEndOfInputException.class);
    }

    @Test
    void testJsonPrimitiveInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("\"primitive\"", enotContext);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("eNot expecting object or array as root JSON node");
    }

    @Test
    void testJsonEmptyObjectInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("{}", enotContext);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(1);
        EnotJsonError error = jsonErrors.get(0);
        assertThat(error.getJsonPointer()).isEqualTo("");
        assertThat(error.getDetails()).isEqualTo("required eNot element field type");
    }

    @Test
    void testJsonEmptyArrayInput() throws Exception {

        EnotParsingException parsingException = assertThrows(EnotParsingException.class, () -> {
            enotParser.parse("[]", enotContext);
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
            enotParser.parse(json, enotContext);
        });

        List<EnotJsonError> jsonErrors = parsingException.getJsonErrors();
        assertThat(jsonErrors).hasSize(4);

        assertThat(isJsonErrorPresent("/body/0/body/0/type", "unsupported type of eNot element: invalid, make sure this type was added to EnotRegistry",
                jsonErrors)).isTrue();
        assertThat(isJsonErrorPresent("/body/0/body/1/type", "eNot element field value type must be string, provided: NULL",
                jsonErrors)).isTrue();
        assertThat(isJsonErrorPresent("/body/0/body", "eNot element body type must be of type ASN1_ELEMENT",
                jsonErrors)).isTrue();
        assertThat(isJsonErrorPresent("/body/1/body/1", "required eNot element field type",
                jsonErrors)).isTrue();
    }

    private boolean isJsonErrorPresent(String jsonPointer, String details, List<EnotJsonError> jsonErrors) {
        return jsonErrors.stream()
                .anyMatch(error -> error.getJsonPointer().equals(jsonPointer)
                        && error.getDetails().equals(details));
    }
}
