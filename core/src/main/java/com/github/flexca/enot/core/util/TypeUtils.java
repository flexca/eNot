package com.github.flexca.enot.core.util;

import com.github.flexca.enot.core.element.value.EnotValueType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

public class TypeUtils {

    private TypeUtils() {
    }

    public static boolean canConsume(EnotValueType that, EnotValueType candidate) {

        if (that.equals(candidate)) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(candidate.getSuperTypes())) {
            for (EnotValueType superType : candidate.getSuperTypes()) {
                if (canConsume(that, superType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean haveSuper(EnotValueType that, EnotValueType superType) {
        return superType.canConsume(that);
    }

    public static boolean haveCyclicDependency(EnotValueType that) {

        Set<EnotValueType> typesInPath = new HashSet<>();
        return haveCyclicDependency(that, typesInPath);
    }

    private static boolean haveCyclicDependency(EnotValueType that, Set<EnotValueType> typesInPath) {

        if (!typesInPath.add(that)) {
            return true;
        }
        for (EnotValueType superType : that.getSuperTypes()) {
            if (haveCyclicDependency(superType, typesInPath)) {
                return true;
            }
        }
        return false;
    }
}
