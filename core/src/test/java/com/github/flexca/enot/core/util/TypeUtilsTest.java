package com.github.flexca.enot.core.util;

import com.github.flexca.enot.core.element.value.CommonEnotValueType;
import com.github.flexca.enot.core.element.value.EnotValueType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeUtilsTest {

    @Test
    void testNoCyclicDependency() {

        EnotValueType typeA = createCustomValueType("A", null);
        EnotValueType typeB = createCustomValueType("B", Set.of(typeA));
        EnotValueType typeC = createCustomValueType("C", Set.of(typeB));

        assertThat(TypeUtils.haveCyclicDependency(typeA)).isFalse();
        assertThat(TypeUtils.haveCyclicDependency(typeB)).isFalse();
        assertThat(TypeUtils.haveCyclicDependency(typeC)).isFalse();
    }

    @Test
    void testCyclicDependencyPresent() {

        Set<EnotValueType> typeASuperTypes = new HashSet<>();
        EnotValueType typeA = createCustomValueType("A", typeASuperTypes);
        EnotValueType typeB = createCustomValueType("B", Set.of(typeA));
        EnotValueType typeC = createCustomValueType("C", Set.of(typeB));
        typeASuperTypes.add(typeC);

        assertThat(TypeUtils.haveCyclicDependency(typeA)).isTrue();
        assertThat(TypeUtils.haveCyclicDependency(typeB)).isTrue();
        assertThat(TypeUtils.haveCyclicDependency(typeC)).isTrue();
    }

    @Test
    void testNoCyclicDependencyForDiamondCase() {

        EnotValueType typeB = createCustomValueType("B", Set.of(CommonEnotValueType.TEXT));
        EnotValueType typeC = createCustomValueType("C", Set.of(CommonEnotValueType.TEXT));
        EnotValueType typeA = createCustomValueType("A", Set.of(typeB, typeC));

        assertThat(TypeUtils.haveCyclicDependency(typeA)).isFalse();
    }

    private EnotValueType createCustomValueType(final String typeName, final Set<EnotValueType> superTypes) {
        return new EnotValueType() {
            @Override
            public String getName() {
                return typeName;
            }

            @Override
            public Set<EnotValueType> getSuperTypes() {
                return superTypes;
            }

            @Override
            public boolean isAllowedForAttributes() {
                return false;
            }
        };
    }
}
