package io.github.flexca.enot.core.element.value;

import io.github.flexca.enot.core.registry.EnotBinaryConverter;
import io.github.flexca.enot.core.util.TypeUtils;

import java.util.Set;

public interface EnotValueType {

    String getName();

    Set<EnotValueType> getSuperTypes();

    boolean isAllowedForAttributes();

    EnotBinaryConverter getBinaryConverter();

    default boolean canConsume(EnotValueType candidate) {
        return TypeUtils.canConsume(this, candidate);
    }

    default boolean haveSuper(EnotValueType superType) {
        return TypeUtils.haveSuper(this, superType);
    }

    default boolean haveCyclicDependency() {
        return TypeUtils.haveCyclicDependency(this);
    }
}
