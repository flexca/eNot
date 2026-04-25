package io.github.flexca.enot.core.serializer;

import io.github.flexca.enot.core.exception.EnotInvalidArgumentException;
import io.github.flexca.enot.core.exception.EnotInvalidConfigurationException;
import io.github.flexca.enot.core.serializer.context.SerializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SerializationContextTest {

    private ObjectMapper jsonObjectMapper;
    private ObjectMapper yamlObjectMapper;

    @BeforeEach
    void init() {
        jsonObjectMapper = new ObjectMapper();
        yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    }

    // --- Builder ---

    @Test
    void buildFromMap() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("subject_cn", "Alice"))
                .build();

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
    }

    @Test
    void buildFromJson() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams("{\"subject_cn\": \"Alice\"}")
                .build();

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
    }

    @Test
    void buildWithSingleParam() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("subject_cn", "Alice")
                .build();

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
    }

    // --- Global params ---

    @Test
    void resolveGlobalParam() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("global.issuer_cn", "My CA")
                .build();

        assertThat(ctx.resolvePlaceholderValue("global.issuer_cn")).isEqualTo("My CA");
    }

    @Test
    void globalParamNotReturnedForPlainLookup() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("global.issuer_cn", "My CA")
                .build();

        assertThat(ctx.resolvePlaceholderValue("issuer_cn")).isNull();
    }

    // --- Path navigation ---

    @Test
    void resolveNestedParamAfterPathStepForward() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("subject", Map.of("cn", "Alice")))
                .build();

        ctx.pathStepForward("subject");

        assertThat(ctx.resolvePlaceholderValue("cn")).isEqualTo("Alice");

        ctx.pathStepBack();
    }

    @Test
    void resolveRootParamAfterPathStepBack() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "subject_cn", "Alice",
                        "subject", Map.of("cn", "Bob")
                ))
                .build();

        ctx.pathStepForward("subject");
        ctx.pathStepBack();

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
    }

    // --- Array / loop iteration ---

    @Test
    void resolveArrayItemAtCurrentIndex() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "dns_names", List.of(
                                Map.of("value", "alice.example.com"),
                                Map.of("value", "bob.example.com")
                        )
                ))
                .build();

        ctx.pathStepForward("dns_names");

        assertThat(ctx.hasNext()).isTrue();
        assertThat(ctx.resolvePlaceholderValue("value")).isEqualTo("alice.example.com");

        ctx.nextIndex();
        assertThat(ctx.hasNext()).isTrue();
        assertThat(ctx.resolvePlaceholderValue("value")).isEqualTo("bob.example.com");

        ctx.nextIndex();
        assertThat(ctx.hasNext()).isFalse();

        ctx.pathStepBack();
    }

    @Test
    void resetIndexRestoresFirstArrayItem() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "items", List.of(
                                Map.of("v", "first"),
                                Map.of("v", "second")
                        )
                ))
                .build();

        ctx.pathStepForward("items");
        ctx.nextIndex();
        assertThat(ctx.resolvePlaceholderValue("v")).isEqualTo("second");

        ctx.resetIndex();
        assertThat(ctx.resolvePlaceholderValue("v")).isEqualTo("first");

        ctx.pathStepBack();
    }

    @Test
    void missingParamReturnsNull() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("subject_cn", "Alice")
                .build();

        assertThat(ctx.resolvePlaceholderValue("missing_key")).isNull();
    }

    // --- hasNext on non-array ---

    @Test
    void hasNextReturnsTrueOnceForMapPath() {
        // A map path is not an array — it represents a single item, so hasNext()
        // should return true once (index 0) and false after nextIndex()
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("subject", Map.of("cn", "Alice")))
                .build();

        ctx.pathStepForward("subject");

        assertThat(ctx.hasNext()).isTrue();
        ctx.nextIndex();
        assertThat(ctx.hasNext()).isFalse();

        ctx.pathStepBack();
    }

    // --- nextIndex past end throws ---

    @Test
    void nextIndexPastEndThrows() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "items", List.of(Map.of("v", "only"))
                ))
                .build();

        ctx.pathStepForward("items");
        ctx.nextIndex(); // moves past the single item
        assertThat(ctx.hasNext()).isFalse();

        assertThatThrownBy(ctx::nextIndex)
                .isInstanceOf(EnotInvalidArgumentException.class);

        ctx.pathStepBack();
    }

    // --- global param visible from nested path ---

    @Test
    void globalParamVisibleInsideNestedPath() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("subject", Map.of("cn", "Alice")))
                .withParam("global.issuer_cn", "My CA")
                .build();

        ctx.pathStepForward("subject");

        assertThat(ctx.resolvePlaceholderValue("global.issuer_cn")).isEqualTo("My CA");

        ctx.pathStepBack();
    }

    // --- global param visible inside array iteration ---

    @Test
    void globalParamVisibleInsideArrayIteration() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("items", List.of(Map.of("v", "first"))))
                .withParam("global.issuer_cn", "My CA")
                .build();

        ctx.pathStepForward("items");

        assertThat(ctx.resolvePlaceholderValue("global.issuer_cn")).isEqualTo("My CA");

        ctx.pathStepBack();
    }

    // --- pathStepBack at root is safe ---

    @Test
    void pathStepBackAtRootIsNoOp() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("subject_cn", "Alice")
                .build();

        ctx.pathStepBack(); // should not throw
        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
    }

    // --- hasNext on root ---

    @Test
    void hasNextAtRootReturnsTrueInitially() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("subject_cn", "Alice")
                .build();

        assertThat(ctx.hasNext()).isTrue();
    }

    // --- Full loop simulation ---

    @Test
    void fullLoopSimulation() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "dns_names", List.of(
                                Map.of("value", "alice.example.com"),
                                Map.of("value", "bob.example.com"),
                                Map.of("value", "carol.example.com")
                        )
                ))
                .build();

        ctx.pathStepForward("dns_names");

        List<String> collected = new java.util.ArrayList<>();
        while (ctx.hasNext()) {
            collected.add((String) ctx.resolvePlaceholderValue("value"));
            ctx.nextIndex();
        }

        ctx.pathStepBack();

        assertThat(collected).containsExactly(
                "alice.example.com",
                "bob.example.com",
                "carol.example.com"
        );
    }

    // --- Nested loop ---

    @Test
    void nestedLoopSimulation() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "subjects", List.of(
                                Map.of("cn", "Alice", "dns_names", List.of(
                                        Map.of("value", "alice1.example.com"),
                                        Map.of("value", "alice2.example.com")
                                )),
                                Map.of("cn", "Bob", "dns_names", List.of(
                                        Map.of("value", "bob.example.com")
                                ))
                        )
                ))
                .build();

        ctx.pathStepForward("subjects");

        List<String> collected = new java.util.ArrayList<>();
        while (ctx.hasNext()) {
            String cn = (String) ctx.resolvePlaceholderValue("cn");

            ctx.pathStepForward("dns_names");
            while (ctx.hasNext()) {
                collected.add(cn + ":" + ctx.resolvePlaceholderValue("value"));
                ctx.nextIndex();
            }
            ctx.pathStepBack();

            ctx.nextIndex();
        }

        ctx.pathStepBack();

        assertThat(collected).containsExactly(
                "Alice:alice1.example.com",
                "Alice:alice2.example.com",
                "Bob:bob.example.com"
        );
    }

    // --- Path step forward into non-existent key ---

    @Test
    void pathStepForwardIntoMissingKeyReturnsNullSafely() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("subject_cn", "Alice"))
                .build();

        ctx.pathStepForward("missing");

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isNull();

        ctx.pathStepBack();
    }

    // --- Reset index and re-iterate ---

    @Test
    void resetIndexAllowsSecondIteration() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "items", List.of(
                                Map.of("v", "first"),
                                Map.of("v", "second")
                        )
                ))
                .build();

        ctx.pathStepForward("items");

        List<String> firstPass = new java.util.ArrayList<>();
        while (ctx.hasNext()) {
            firstPass.add((String) ctx.resolvePlaceholderValue("v"));
            ctx.nextIndex();
        }

        ctx.resetIndex();

        List<String> secondPass = new java.util.ArrayList<>();
        while (ctx.hasNext()) {
            secondPass.add((String) ctx.resolvePlaceholderValue("v"));
            ctx.nextIndex();
        }

        ctx.pathStepBack();

        assertThat(firstPass).containsExactly("first", "second");
        assertThat(secondPass).containsExactly("first", "second");
    }

    // --- Numeric and boolean param types ---

    @Test
    void numericParamPreservesType() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("count", 42)
                .build();

        Object value = ctx.resolvePlaceholderValue("count");
        assertThat(value).isInstanceOf(Integer.class);
        assertThat(value).isEqualTo(42);
    }

    @Test
    void booleanParamPreservesType() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParam("is_ca", true)
                .build();

        Object value = ctx.resolvePlaceholderValue("is_ca");
        assertThat(value).isInstanceOf(Boolean.class);
        assertThat(value).isEqualTo(true);
    }

    @Test
    void numericParamFromJsonPreservesType() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams("{\"count\": 42}")
                .build();

        Object value = ctx.resolvePlaceholderValue("count");
        assertThat(value).isInstanceOf(Integer.class);
        assertThat(value).isEqualTo(42);
    }

    // --- Empty array ---

    @Test
    void emptyArrayHasNextReturnsFalseImmediately() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("items", List.of()))
                .build();

        ctx.pathStepForward("items");

        assertThat(ctx.hasNext()).isFalse();

        ctx.pathStepBack();
    }

    // --- Outer loop second iteration resolves correctly ---

    @Test
    void outerLoopSecondIterationResolvesCorrectly() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "subjects", List.of(
                                Map.of("cn", "Alice"),
                                Map.of("cn", "Bob"),
                                Map.of("cn", "Carol")
                        )
                ))
                .build();

        ctx.pathStepForward("subjects");

        assertThat(ctx.resolvePlaceholderValue("cn")).isEqualTo("Alice");
        ctx.nextIndex();
        assertThat(ctx.resolvePlaceholderValue("cn")).isEqualTo("Bob");
        ctx.nextIndex();
        assertThat(ctx.resolvePlaceholderValue("cn")).isEqualTo("Carol");

        ctx.pathStepBack();
    }

    // --- Primitive array items — not supported, always returns null ---

    @Test
    void primitiveArrayItemReturnsNull() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "dns_names", List.of("alice.example.com", "bob.example.com")
                ))
                .build();

        ctx.pathStepForward("dns_names");

        assertThat(ctx.hasNext()).isTrue();
        assertThat(ctx.resolvePlaceholderValue("value")).isNull();

        ctx.pathStepBack();
    }

    // --- Nested loop with both outer items having multiple inner items ---

    @Test
    void nestedLoopBothOuterItemsHaveMultipleInnerItems() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "subjects", List.of(
                                Map.of("cn", "Alice", "sans", List.of(
                                        Map.of("value", "alice1.com"),
                                        Map.of("value", "alice2.com")
                                )),
                                Map.of("cn", "Bob", "sans", List.of(
                                        Map.of("value", "bob1.com"),
                                        Map.of("value", "bob2.com")
                                ))
                        )
                ))
                .build();

        ctx.pathStepForward("subjects");

        List<String> collected = new java.util.ArrayList<>();
        while (ctx.hasNext()) {
            String cn = (String) ctx.resolvePlaceholderValue("cn");
            ctx.pathStepForward("sans");
            while (ctx.hasNext()) {
                collected.add(cn + ":" + ctx.resolvePlaceholderValue("value"));
                ctx.nextIndex();
            }
            ctx.pathStepBack();
            ctx.nextIndex();
        }

        ctx.pathStepBack();

        assertThat(collected).containsExactly(
                "Alice:alice1.com",
                "Alice:alice2.com",
                "Bob:bob1.com",
                "Bob:bob2.com"
        );
    }

    // --- YAML string params ---

    @Test
    void buildFromYamlString() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withYamlObjectMapper(yamlObjectMapper)
                .withParams("subject_cn: Alice\ncountry: PL")
                .build();

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
        assertThat(ctx.resolvePlaceholderValue("country")).isEqualTo("PL");
    }

    // --- Global param auto-extracted from withParams ---

    @Test
    void globalParamAutoExtractedFromMap() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of(
                        "subject_cn", "Alice",
                        "global.issuer", "My CA"
                ))
                .build();

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
        assertThat(ctx.resolvePlaceholderValue("global.issuer")).isEqualTo("My CA");
        assertThat(ctx.resolvePlaceholderValue("issuer")).isNull();
    }

    @Test
    void globalParamAutoExtractedFromJsonString() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams("{\"subject_cn\": \"Alice\", \"global.issuer\": \"My CA\"}")
                .build();

        assertThat(ctx.resolvePlaceholderValue("subject_cn")).isEqualTo("Alice");
        assertThat(ctx.resolvePlaceholderValue("global.issuer")).isEqualTo("My CA");
    }

    // --- Multiple withParams calls and mixing ---

    @Test
    void multipleWithParamsMapCallsAreMerged() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("cn", "Alice"))
                .withParams(Map.of("country", "PL"))
                .build();

        assertThat(ctx.resolvePlaceholderValue("cn")).isEqualTo("Alice");
        assertThat(ctx.resolvePlaceholderValue("country")).isEqualTo("PL");
    }

    @Test
    void withParamAndWithParamsMapCanBeMixed() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams(Map.of("cn", "Alice"))
                .withParam("country", "PL")
                .build();

        assertThat(ctx.resolvePlaceholderValue("cn")).isEqualTo("Alice");
        assertThat(ctx.resolvePlaceholderValue("country")).isEqualTo("PL");
    }

    // --- Builder validation failures ---

    @Test
    void withParamBlankKeyThrows() {
        assertThatThrownBy(() ->
                new SerializationContext.Builder()
                        .withJsonObjectMapper(jsonObjectMapper)
                        .withParam("", "value")
        ).isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void withParamInvalidNameThrows() {
        assertThatThrownBy(() ->
                new SerializationContext.Builder()
                        .withJsonObjectMapper(jsonObjectMapper)
                        .withParam("invalid name", "value")
        ).isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void withParamSystemVariableThrows() {
        assertThatThrownBy(() ->
                new SerializationContext.Builder()
                        .withJsonObjectMapper(jsonObjectMapper)
                        .withParam("system.some_var", "value")
        ).isInstanceOf(EnotInvalidArgumentException.class);
    }

    @Test
    void withParamsJsonStringWithoutJsonMapperThrows() {
        assertThatThrownBy(() ->
                new SerializationContext.Builder()
                        .withYamlObjectMapper(yamlObjectMapper)
                        .withParams("{\"cn\": \"Alice\"}")
        ).isInstanceOf(EnotInvalidConfigurationException.class);
    }

    @Test
    void withParamsYamlStringWithoutYamlMapperThrows() {
        assertThatThrownBy(() ->
                new SerializationContext.Builder()
                        .withJsonObjectMapper(jsonObjectMapper)
                        .withParams("cn: Alice")
        ).isInstanceOf(EnotInvalidConfigurationException.class);
    }

    @Test
    void withParamsBlankStringThrows() {
        assertThatThrownBy(() ->
                new SerializationContext.Builder()
                        .withJsonObjectMapper(jsonObjectMapper)
                        .withParams("   ")
        ).isInstanceOf(EnotInvalidArgumentException.class);
    }

    // --- Type preservation from JSON ---

    @Test
    void booleanParamFromJsonPreservesType() {
        SerializationContext ctx = new SerializationContext.Builder()
                .withJsonObjectMapper(jsonObjectMapper)
                .withParams("{\"is_ca\": true}")
                .build();

        Object value = ctx.resolvePlaceholderValue("is_ca");
        assertThat(value).isInstanceOf(Boolean.class);
        assertThat(value).isEqualTo(true);
    }
}
