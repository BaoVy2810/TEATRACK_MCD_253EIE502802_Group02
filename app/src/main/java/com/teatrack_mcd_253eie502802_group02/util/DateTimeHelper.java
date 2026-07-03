package com.teatrack_mcd_253eie502802_group02.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Shared date helpers — ISO 8601 UTC, aligned with orders / blog / promotions.
 */
public final class DateTimeHelper {

    private static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String[] ISO_PARSE_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
    };

    private DateTimeHelper() {
    }

    /** e.g. 2026-06-29T07:09:24.111Z */
    public static String isoNow() {
        SimpleDateFormat formatter = new SimpleDateFormat(ISO_PATTERN, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }

    public static String isoFromMillis(long millis) {
        SimpleDateFormat formatter = new SimpleDateFormat(ISO_PATTERN, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date(millis));
    }

    /** Supports legacy numeric millis stored as number or digit-only string. */
    public static long toEpochMillis(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return 0L;
        }
        String trimmed = createdAt.trim();
        if (trimmed.matches("\\d+")) {
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        for (String pattern : ISO_PARSE_PATTERNS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(trimmed);
                return date != null ? date.getTime() : 0L;
            } catch (ParseException ignored) {
                // try next pattern
            }
        }
        return 0L;
    }

    public static String formatDisplayDate(String createdAt, String pattern) {
        long millis = toEpochMillis(createdAt);
        if (millis <= 0) {
            return "";
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(millis));
    }
}
