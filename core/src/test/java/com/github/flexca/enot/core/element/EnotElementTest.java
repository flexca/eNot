package com.github.flexca.enot.core.element;

import com.github.flexca.enot.core.types.asn1.attribute.Asn1Attribute;
import com.github.flexca.enot.core.types.system.attribute.SystemAttribute;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnotElementTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private EnotElement primitive(String type, String body) {
        EnotElement e = new EnotElement();
        e.setType(type);
        e.setAttributes(Map.of(Asn1Attribute.TAG, "utf8_string"));
        e.setBody(body);
        return e;
    }

    // -----------------------------------------------------------------------
    // copy — primitive body
    // -----------------------------------------------------------------------

    @Test
    void testCopyWithPrimitiveBodyProducesEqualElement() {
        EnotElement original = primitive("asn.1", "${subject_cn}");

        EnotElement copy = original.copy();

        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    void testCopyWithPrimitiveBodyIsIndependentFromOriginal() {
        EnotElement original = primitive("asn.1", "${subject_cn}");

        EnotElement copy = original.copy();
        copy.setBody("mutated");
        copy.setType("other");

        assertThat(original.getBody()).isEqualTo("${subject_cn}");
        assertThat(original.getType()).isEqualTo("asn.1");
    }

    @Test
    void testCopyAttributeMapIsIndependentFromOriginal() {
        EnotElement original = primitive("asn.1", "value");

        EnotElement copy = original.copy();
        copy.getAttributes().put(Asn1Attribute.TAG, "sequence");

        assertThat(original.getAttributes().get(Asn1Attribute.TAG)).isEqualTo("utf8_string");
        assertThat(copy.getAttributes().get(Asn1Attribute.TAG)).isEqualTo("sequence");
    }

    @Test
    void testCopyPreservesOptionalFlag() {
        EnotElement original = primitive("asn.1", "${cn}");
        original.setOptional(true);

        EnotElement copy = original.copy();

        assertThat(copy.isOptional()).isTrue();
    }

    // -----------------------------------------------------------------------
    // copy — nested EnotElement body
    // -----------------------------------------------------------------------

    @Test
    void testCopyWithNestedElementBodyProducesEqualElement() {
        EnotElement inner = primitive("asn.1", "${value}");
        EnotElement outer = new EnotElement();
        outer.setType("asn.1");
        outer.setAttributes(Map.of(Asn1Attribute.TAG, "sequence"));
        outer.setBody(inner);

        EnotElement copy = outer.copy();

        assertThat(copy).isEqualTo(outer);
        assertThat(copy.getBody()).isNotSameAs(inner);
    }

    @Test
    void testCopyWithNestedElementBodyIsDeep() {
        EnotElement inner = primitive("asn.1", "${value}");
        EnotElement outer = new EnotElement();
        outer.setType("asn.1");
        outer.setAttributes(Map.of(Asn1Attribute.TAG, "sequence"));
        outer.setBody(inner);

        EnotElement copy = outer.copy();
        ((EnotElement) copy.getBody()).setBody("mutated");

        assertThat(inner.getBody()).isEqualTo("${value}");
    }

    // -----------------------------------------------------------------------
    // copy — list body
    // -----------------------------------------------------------------------

    @Test
    void testCopyWithListBodyProducesEqualElement() {
        EnotElement child1 = primitive("asn.1", "${cn}");
        EnotElement child2 = primitive("asn.1", "${ou}");
        EnotElement parent = new EnotElement();
        parent.setType("asn.1");
        parent.setAttributes(Map.of(Asn1Attribute.TAG, "sequence"));
        parent.setBody(List.of(child1, child2));

        EnotElement copy = parent.copy();

        assertThat(copy).isEqualTo(parent);
    }

    @Test
    void testCopyWithListBodyIsDeep() {
        EnotElement child = primitive("asn.1", "${cn}");
        EnotElement parent = new EnotElement();
        parent.setType("asn.1");
        parent.setAttributes(Map.of(Asn1Attribute.TAG, "sequence"));
        parent.setBody(List.of(child));

        EnotElement copy = parent.copy();

        @SuppressWarnings("unchecked")
        List<Object> copiedBody = (List<Object>) copy.getBody();
        assertThat(copiedBody.get(0)).isNotSameAs(child);
        ((EnotElement) copiedBody.get(0)).setBody("mutated");
        assertThat(child.getBody()).isEqualTo("${cn}");
    }

    @Test
    void testCopyWithMixedListBodyPreservesPrimitives() {
        // System element with a mixed body: one EnotElement + one primitive placeholder string
        EnotElement childElement = primitive("asn.1", "${value}");
        EnotElement parent = new EnotElement();
        parent.setType("system");
        parent.setAttributes(Map.of(SystemAttribute.KIND, "group"));
        parent.setBody(List.of(childElement, "${extra}"));

        EnotElement copy = parent.copy();

        @SuppressWarnings("unchecked")
        List<Object> copiedBody = (List<Object>) copy.getBody();
        assertThat(copiedBody).hasSize(2);
        assertThat(copiedBody.get(0)).isInstanceOf(EnotElement.class);
        assertThat(copiedBody.get(0)).isNotSameAs(childElement);
        assertThat(copiedBody.get(1)).isEqualTo("${extra}");
    }

    // -----------------------------------------------------------------------
    // equals / hashCode
    // -----------------------------------------------------------------------

    @Test
    void testEqualsAndHashCodeForIdenticalElements() {
        EnotElement a = primitive("asn.1", "2.5.4.3");
        EnotElement b = primitive("asn.1", "2.5.4.3");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void testNotEqualWhenTypeDiffers() {
        EnotElement a = primitive("asn.1", "value");
        EnotElement b = primitive("system", "value");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void testNotEqualWhenBodyDiffers() {
        EnotElement a = primitive("asn.1", "foo");
        EnotElement b = primitive("asn.1", "bar");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void testNotEqualWhenOptionalDiffers() {
        EnotElement a = primitive("asn.1", "${v}");
        EnotElement b = primitive("asn.1", "${v}");
        b.setOptional(true);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void testNotEqualWhenAttributesDiffer() {
        EnotElement a = primitive("asn.1", "${v}");
        EnotElement b = new EnotElement();
        b.setType("asn.1");
        b.setAttributes(Map.of(Asn1Attribute.TAG, "sequence"));
        b.setBody("${v}");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void testEqualsSelf() {
        EnotElement a = primitive("asn.1", "value");
        assertThat(a).isEqualTo(a);
    }

    @Test
    void testNotEqualsNull() {
        EnotElement a = primitive("asn.1", "value");
        assertThat(a).isNotEqualTo(null);
    }
}
