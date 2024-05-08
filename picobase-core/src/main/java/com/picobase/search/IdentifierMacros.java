package com.picobase.search;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

public class IdentifierMacros {

    private static final LocalDateTime now = LocalDateTime.now();

    private static final Map<String, Macro> macros = new HashMap<>();

    static {
        macros.put("@now", () -> now);
        macros.put("@second", () -> now.get(ChronoField.SECOND_OF_MINUTE));
        macros.put("@minute", () -> now.get(ChronoField.MINUTE_OF_HOUR));
        macros.put("@hour", () -> now.get(ChronoField.HOUR_OF_DAY));
        macros.put("@day", () -> now.get(ChronoField.DAY_OF_MONTH));
        macros.put("@month", () -> now.get(ChronoField.MONTH_OF_YEAR));
        macros.put("@weekday", () -> now.get(ChronoField.DAY_OF_WEEK));
        macros.put("@year", () -> now.get(ChronoField.YEAR));
        macros.put("@todayStart", () -> {
            LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
            return start;
        });
        macros.put("@todayEnd", () -> {
            LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59, 999999999);
            return end;
        });
        macros.put("@monthStart", () -> {
            LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0, 0);
            return start;
        });
        macros.put("@monthEnd", () -> {
            LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 23, 59, 59, 999999999);
            end = end.plusMonths(1).minusDays(1);
            return end;
        });
        macros.put("@yearStart", () -> {
            LocalDateTime start = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
            return start;
        });
        macros.put("@yearEnd", () -> {
            LocalDateTime end = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59, 999999999);
            return end;
        });
    }

    public static Macro getMacro(String macroName) {
        return macros.get(macroName);
    }

    public interface Macro {
        Object getValue();
    }
}
