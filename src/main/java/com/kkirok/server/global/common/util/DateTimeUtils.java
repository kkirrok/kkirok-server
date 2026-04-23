package com.kkirok.server.global.common.util;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static String toDayLabel(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.KOREAN);
    }

    public static int toDayOfWeekNumber(LocalDate date) {
        return date.getDayOfWeek().getValue() % 7;
    }
}
