package io.github.flexca.enot.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class IpAddressUtils {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    // standard literal-only IPv6 pattern (no hostnames), optionally with an embedded IPv4 tail
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(" +
                    "([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}|" +
                    "([0-9A-Fa-f]{1,4}:){1,7}:|" +
                    "([0-9A-Fa-f]{1,4}:){1,6}:[0-9A-Fa-f]{1,4}|" +
                    "([0-9A-Fa-f]{1,4}:){1,5}(:[0-9A-Fa-f]{1,4}){1,2}|" +
                    "([0-9A-Fa-f]{1,4}:){1,4}(:[0-9A-Fa-f]{1,4}){1,3}|" +
                    "([0-9A-Fa-f]{1,4}:){1,3}(:[0-9A-Fa-f]{1,4}){1,4}|" +
                    "([0-9A-Fa-f]{1,4}:){1,2}(:[0-9A-Fa-f]{1,4}){1,5}|" +
                    "[0-9A-Fa-f]{1,4}:((:[0-9A-Fa-f]{1,4}){1,6})|" +
                    ":((:[0-9A-Fa-f]{1,4}){1,7}|:)|" +
                    "([0-9A-Fa-f]{1,4}:){1,4}:((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|" +
                    "::((:[0-9A-Fa-f]{1,4}){0,5}:)?((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
                    ")$");

    private IpAddressUtils() {
    }

    /**
     * Returns {@code true} if {@code input} is a literal IPv4 or IPv6 address.
     *
     * <p>Hostnames are deliberately rejected: the caller must not pass this value to
     * DNS-resolving APIs (e.g. {@link java.net.InetAddress#getByName}) for anything other
     * than a confirmed literal address, to avoid triggering unwanted name resolution.</p>
     */
    public static boolean isValidIpAddress(String input) {

        if (StringUtils.isBlank(input)) {
            return false;
        }

        return IPV4_PATTERN.matcher(input).matches() || IPV6_PATTERN.matcher(input).matches();
    }
}
