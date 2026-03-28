package com.github.flexca.enot.core.struct.value;

import com.github.flexca.enot.core.util.TypeUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

public interface EnotValueType {

    String getName();

    Set<EnotValueType> getSuperTypes();

    boolean isAllowedForAttributes();

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
