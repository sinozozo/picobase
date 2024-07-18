package com.picobase.search;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

public class IdentifierMacros {


    private static final Map<String, Macro> macros = new HashMap<>();

    static {
        macros.put("@now", () -> LocalDateTime.now());
        macros.put("@second", () -> LocalDateTime.now().get(ChronoField.SECOND_OF_MINUTE));
        macros.put("@minute", () -> LocalDateTime.now().get(ChronoField.MINUTE_OF_HOUR));
        macros.put("@hour", () -> LocalDateTime.now().get(ChronoField.HOUR_OF_DAY));
        macros.put("@day", () -> LocalDateTime.now().get(ChronoField.DAY_OF_MONTH));
        macros.put("@month", () -> LocalDateTime.now().get(ChronoField.MONTH_OF_YEAR));
        macros.put("@weekday", () -> LocalDateTime.now().get(ChronoField.DAY_OF_WEEK));
        macros.put("@year", () -> LocalDateTime.now().get(ChronoField.YEAR));
        macros.put("@todayStart", () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
            return start;
        });
        macros.put("@todayEnd", () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59, 999999999);
            return end;
        });
        macros.put("@monthStart", () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0, 0);
            return start;
        });
        macros.put("@monthEnd", () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 23, 59, 59, 999999999);
            end = end.plusMonths(1).minusDays(1);
            return end;
        });
        macros.put("@yearStart", () -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
            return start;
        });
        macros.put("@yearEnd", () -> {
            LocalDateTime now = LocalDateTime.now();
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
