package com.github.flexca.enot.core.parser;

import com.github.flexca.enot.core.EnotContext;
import com.github.flexca.enot.core.element.EnotElement;
import com.github.flexca.enot.core.expression.ConditionExpressionEvaluator;
import com.github.flexca.enot.core.expression.ConditionExpressionParser;
import com.github.flexca.enot.core.registry.EnotRegistry;
import com.github.flexca.enot.core.serializer.EnotSerializer;
import com.github.flexca.enot.core.testutil.ResourceReaderTestUtils;
import com.github.flexca.enot.core.testutil.TestResourcesReferenceResolver;
import com.github.flexca.enot.core.types.asn1.Asn1TypeSpecification;
import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.types.system.SystemTypeSpecification;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.flexca.enot.core.exception.EnotParsingException;

public class EnotParserReferenceResolverTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private EnotContext enotContext;

    private EnotParser enotParser;

    @BeforeEach
    void init() {
        EnotRegistry enotRegistry = new EnotRegistry.Builder()
                .withTypeSpecifications(new SystemTypeSpecification(), new Asn1TypeSpecification())
                .withElementReferenceResolver(new TestResourcesReferenceResolver())
                .build();
        ConditionExpressionParser conditionExpressionParser = new ConditionExpressionParser();
        enotParser = new EnotParser(objectMapper);
        enotContext = new EnotContext(enotRegistry, enotParser, new EnotSerializer(enotParser),
                conditionExpressionParser, new ConditionExpressionEvaluator(enotRegistry, conditionExpressionParser));
    }

    @Test
    void testReferenceResolvedAtParseTime() throws Exception {

        String json = ResourceReaderTestUtils.readResourceFileAsString("json/asn1/rfc/san/extension-san.json");

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        // Root: single asn.1 SEQUENCE
        assertThat(actual).hasSize(1);
        EnotElement root = actual.get(0);
        assertThat(root.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(root.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));
        assertThat(root.getBody()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<EnotElement> rootBody = (List<EnotElement>) root.getBody();

        // 3 children: OID, optional BOOLEAN, OCTET_STRING
        assertThat(rootBody).hasSize(3);

        // [0] OID "2.5.29.17"
        EnotElement oidElement = rootBody.get(0);
        assertThat(oidElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(oidElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "object_identifier"));
        assertThat(oidElement.getBody()).isEqualTo("2.5.29.17");

        // [1] optional BOOLEAN placeholder
        EnotElement boolElement = rootBody.get(1);
        assertThat(boolElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(boolElement.isOptional()).isTrue();
        assertThat(boolElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "boolean"));
        assertThat(boolElement.getBody()).isEqualTo("${san_critical}");

        // [2] OCTET_STRING wrapping a SEQUENCE wrapping a GROUP("san")
        EnotElement octetStringElement = rootBody.get(2);
        assertThat(octetStringElement.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(octetStringElement.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "octet_string"));

        EnotElement outerSequence = (EnotElement) octetStringElement.getBody();
        assertThat(outerSequence.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(outerSequence.getAttributes()).isEqualTo(Map.of(Asn1Attribute.TAG, "sequence"));

        // The GROUP("san") element
        EnotElement groupElement = (EnotElement) outerSequence.getBody();
        assertThat(groupElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(groupElement.getAttribute(SystemAttribute.KIND)).isEqualTo("group");
        assertThat(groupElement.getAttribute(SystemAttribute.GROUP_NAME)).isEqualTo("san");

        // GROUP body: the REFERENCE element's body has been replaced at parse time
        // with the resolved List<EnotElement> from san-dns.json.
        @SuppressWarnings("unchecked")
        List<EnotElement> groupBody = (List<EnotElement>) groupElement.getBody();
        assertThat(groupBody).hasSize(1);

        // The element in the GROUP body is still typed "reference" — but its body is now
        // the resolved content (no longer a raw JSON structure, resolution happened at parse time).
        EnotElement referenceElement = groupBody.get(0);
        assertThat(referenceElement.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(referenceElement.getAttribute(SystemAttribute.KIND)).isEqualTo("reference");
        assertThat(referenceElement.getAttribute(SystemAttribute.REFERENCE_TYPE)).isEqualTo("test_resources");
        assertThat(referenceElement.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER))
                .isEqualTo("json/asn1/rfc/san/san-dns.json");

        // The body of the REFERENCE element is the resolved list from san-dns.json
        assertThat(referenceElement.getBody()).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<EnotElement> resolvedBody = (List<EnotElement>) referenceElement.getBody();
        assertThat(resolvedBody).hasSize(1);

        // san-dns.json parses to a single system LOOP element
        EnotElement resolvedLoop = resolvedBody.get(0);
        assertThat(resolvedLoop.getType()).isEqualTo(SystemTypeSpecification.TYPE_NAME);
        assertThat(resolvedLoop.getAttribute(SystemAttribute.KIND)).isEqualTo("loop");
        assertThat(resolvedLoop.getAttribute(SystemAttribute.ITEMS_NAME)).isEqualTo("dns_name");
        assertThat(resolvedLoop.isOptional()).isTrue();
        // The loop body is the tagged_object element — not another REFERENCE
        assertThat(resolvedLoop.getBody()).isInstanceOf(EnotElement.class);
        EnotElement taggedObject = (EnotElement) resolvedLoop.getBody();
        assertThat(taggedObject.getType()).isEqualTo(Asn1TypeSpecification.TYPE_NAME);
        assertThat(taggedObject.getAttribute(Asn1Attribute.TAG)).isEqualTo("tagged_object");
    }

    @Test
    void testCyclicDependencyAtoB() throws Exception {
        // A → B → A: the reference element inside cyclic-a.json has identifier
        // "test_resources:json/cyclic/cyclic-b.json". When cyclic-b.json is then
        // parsed and tries to resolve cyclic-a.json, that succeeds (adds cyclic-a).
        // On the second round cyclic-a.json tries to resolve cyclic-b.json again
        // — but cyclic-b is already in the ParsingContext, so the cycle is detected.
        String json = ResourceReaderTestUtils.readResourceFileAsString("json/cyclic/cyclic-a.json");

        assertThatThrownBy(() -> enotParser.parse(json, enotContext))
                .isInstanceOf(EnotParsingException.class)
                .hasMessageContaining("cyclic dependency detected")
                .hasMessageContaining("test_resources:json/cyclic/cyclic-b.json");
    }

    @Test
    void testCyclicDependencySelfReference() throws Exception {
        // A → A: a template that references itself.
        // On the first recursion the identifier is already present in the context.
        String json = ResourceReaderTestUtils.readResourceFileAsString("json/cyclic/self-ref.json");

        assertThatThrownBy(() -> enotParser.parse(json, enotContext))
                .isInstanceOf(EnotParsingException.class)
                .hasMessageContaining("cyclic dependency detected")
                .hasMessageContaining("test_resources:json/cyclic/self-ref.json");
    }

    @Test
    void testDiamondDependencyNoCycle() throws Exception {
        // Diamond: root → left → leaf
        //                 → right → leaf
        // Each branch gets an independent ParsingContext copy, so resolving
        // "leaf" from two sibling branches does not trigger a cycle error.
        String json = ResourceReaderTestUtils.readResourceFileAsString("json/cyclic/diamond-root.json");

        List<EnotElement> actual = enotParser.parse(json, enotContext);

        // root is an array → two reference elements
        assertThat(actual).hasSize(2);

        EnotElement leftRef = actual.get(0);
        assertThat(leftRef.getAttribute(SystemAttribute.KIND)).isEqualTo("reference");
        assertThat(leftRef.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER))
                .isEqualTo("json/cyclic/diamond-left.json");
        // leftRef.body = [ref-to-leaf];  ref-to-leaf.body = [oid]
        assertThat(leftRef.getBody()).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<EnotElement> leftBody = (List<EnotElement>) leftRef.getBody();
        assertThat(leftBody).hasSize(1);
        EnotElement leftLeafRef = leftBody.get(0);
        assertThat(leftLeafRef.getAttribute(SystemAttribute.KIND)).isEqualTo("reference");
        assertThat(leftLeafRef.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER))
                .isEqualTo("json/cyclic/diamond-leaf.json");
        assertThat(leftLeafRef.getBody()).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<EnotElement> leftLeafBody = (List<EnotElement>) leftLeafRef.getBody();
        assertThat(leftLeafBody).hasSize(1);
        assertThat(leftLeafBody.get(0).getAttribute(Asn1Attribute.TAG)).isEqualTo("object_identifier");

        EnotElement rightRef = actual.get(1);
        assertThat(rightRef.getAttribute(SystemAttribute.KIND)).isEqualTo("reference");
        assertThat(rightRef.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER))
                .isEqualTo("json/cyclic/diamond-right.json");
        // rightRef.body = [ref-to-leaf];  ref-to-leaf.body = [oid]
        assertThat(rightRef.getBody()).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<EnotElement> rightBody = (List<EnotElement>) rightRef.getBody();
        assertThat(rightBody).hasSize(1);
        EnotElement rightLeafRef = rightBody.get(0);
        assertThat(rightLeafRef.getAttribute(SystemAttribute.KIND)).isEqualTo("reference");
        assertThat(rightLeafRef.getAttribute(SystemAttribute.REFERENCE_IDENTIFIER))
                .isEqualTo("json/cyclic/diamond-leaf.json");
        assertThat(rightLeafRef.getBody()).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<EnotElement> rightLeafBody = (List<EnotElement>) rightLeafRef.getBody();
        assertThat(rightLeafBody).hasSize(1);
        assertThat(rightLeafBody.get(0).getAttribute(Asn1Attribute.TAG)).isEqualTo("object_identifier");
    }
}
