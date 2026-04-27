package io.github.flexca.enot.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

public class PlaceholderUtils {

    public static final String GLOBAL_PARAM = "global";
    public static final String GLOBAL_PARAM_PREFIX = GLOBAL_PARAM + ".";

    private static final Pattern VARIABLE_NAME_REGEXP = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

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

    public static Optional<String> extractPlaceholder(Object input) {

        if (!isPlaceholder(input)) {
            return Optional.empty();
        }
        String stringInput = (String) input;
        return Optional.of(stringInput.substring(2, stringInput.length() - 1));
    }

    public static boolean isValidVariableName(String input) {
        return isValidVariableName(input, true);
    }

    public static boolean isValidVariableName(String input, boolean takeIntoAccountPrefixes) {

        if(StringUtils.isBlank(input)) {
            return false;
        }

        String inputWithoutPrefix;
        if(takeIntoAccountPrefixes) {
            if (input.startsWith(GLOBAL_PARAM_PREFIX)) {
                inputWithoutPrefix = input.substring(GLOBAL_PARAM_PREFIX.length());
            } else {
                inputWithoutPrefix = input;
            }
        } else {
            inputWithoutPrefix = input;
        }

        return VARIABLE_NAME_REGEXP.matcher(inputWithoutPrefix).matches();
    }

    public static boolean isGlobalVariable(String input) {

        if(StringUtils.isBlank(input)) {
            return false;
        }

        return input.startsWith(GLOBAL_PARAM_PREFIX);
    }

}
