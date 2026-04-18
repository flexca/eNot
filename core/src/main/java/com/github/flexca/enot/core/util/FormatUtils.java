package com.github.flexca.enot.core.util;

import com.github.flexca.enot.core.parser.EnotInputFormat;
import org.apache.commons.lang3.StringUtils;

public class FormatUtils {

    private FormatUtils() {
    }

    public static EnotInputFormat detectInputFormat(String input) {

        if(StringUtils.isBlank(input)) {
            return EnotInputFormat.UNSUPPORTED;
        }

        String trimmedInput = input.stripIndent().trim();
        if (trimmedInput.startsWith("{") || trimmedInput.startsWith("[")) {
            return EnotInputFormat.JSON;
        } else {
            return EnotInputFormat.YAML;
        }
    }
}
