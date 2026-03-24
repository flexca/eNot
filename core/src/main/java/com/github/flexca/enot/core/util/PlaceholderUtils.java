package com.github.flexca.enot.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class PlaceholderUtils {

    private PlaceholderUtils() {
    }

    public static boolean isPlaceholder(Object input) {

        if (input instanceof String stringInput) {
            return isPlaceholder(stringInput);
        }
        return false;
    }

    public static boolean isPlaceholder(String input) {

        if (StringUtils.isBlank(input)) {
            return false;
        }
        if (input.length() < 4) {
            return false;
        }
        if (!input.startsWith("${") || !input.endsWith("}")) {
            return false;
        }
        return true;
    }

}
