package com.github.flexca.enot.core.struct.value;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

public interface EnotValueType {

    String getName();

    Set<EnotValueType> getSuperTypes();

    default boolean canConsume(EnotValueType candidate) {

        if (this.equals(candidate)) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(candidate.getSuperTypes())) {
            for (EnotValueType superType : candidate.getSuperTypes()) {
                if (canConsume(superType)) {
                    return true;
                }
            }
        }
        return false;
    }

    default boolean haveSuper(EnotValueType superType) {

        return superType.canConsume(this);
    }
}
