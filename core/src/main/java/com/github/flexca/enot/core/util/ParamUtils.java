package com.github.flexca.enot.core.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParamUtils {

    private ParamUtils() {
    }

    public static Optional<Object> extractParamsByPath(Map<String, Object> parameters, List<String> pathStack) {

        Map<?, ?> currentParams = parameters;
        for (int i = 0; i < pathStack.size(); i++) {
            String path = pathStack.get(i);
            Object subParams = currentParams.get(path);
            if (i == pathStack.size() - 1) {
                return subParams == null ? Optional.empty() : Optional.of(subParams);
            }
            if (subParams instanceof Map<?, ?> mapSubParams) {
                currentParams = mapSubParams;
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
