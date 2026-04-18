package com.github.flexca.enot.core.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {

    private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    private static final DateTimeFormatter DATE_TIME_FORMATTER_UTC  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final DateTimeFormatter DATE_TIME_FORMATTER_ASN1  = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'");

    private DateTimeUtils() {
    }

    public static boolean isValidDateTime(Object input) {
        return false;
    }

    public static ZonedDateTime toZonedDateTime(String input) {
        if(StringUtils.isBlank(input)) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(input, DATE_TIME_FORMATTER_UTC);
        return ZonedDateTime.of(localDateTime, UTC_ZONE_ID);
    }

    public static String formatForAsn1(ZonedDateTime input) {
        return input == null ? null : input.format(DATE_TIME_FORMATTER_ASN1);
    }

    public static ZonedDateTime toZonedDateTime(Date input) {
        return input == null ? null : ZonedDateTime.ofInstant(input.toInstant(), UTC_ZONE_ID);
    }

    public static String format(ZonedDateTime input) {
        return input == null ? null : input.format(DATE_TIME_FORMATTER_UTC);
    }
}
