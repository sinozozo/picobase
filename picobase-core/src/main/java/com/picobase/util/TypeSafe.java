package com.picobase.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 类型安全转换
 *
 * @author : clown
 * @date : 2024-03-04 13:38
 **/
public class TypeSafe {
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public static final DateTimeFormatter DATE_FM = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATETIMESSS_FM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public TypeSafe() {
    }

    public static String anyToString(Object v) {
        if (v == null) {
            return null;
        } else {
            return v instanceof String ? (String) v : v.toString();
        }
    }


    public static String anyToString(Object v, String defaultValue) {
        if (v == null) {
            return defaultValue;
        } else {
            return v instanceof String ? (String) v : v.toString();
        }
    }

    public static int anyToInt(Object v) {
        return anyToInt(v, 0);
    }

    public static int anyToInt(Object v, int defaultValue) {
        if (v == null) {
            return defaultValue;
        } else if (v instanceof Integer) {
            return (Integer) v;
        } else if (v instanceof Number) {
            return ((Number) v).intValue();
        } else {
            try {
                return Integer.parseInt(v.toString());
            } catch (Throwable var3) {
                return defaultValue;
            }
        }
    }

    public static long anyToLong(Object v) {
        return anyToLong(v, 0L);
    }

    public static long anyToLong(Object v, long defaultValue) {
        if (v == null) {
            return defaultValue;
        } else if (v instanceof Integer) {
            return (long) (Integer) v;
        } else if (v instanceof Long) {
            return (Long) v;
        } else if (v instanceof Number) {
            return ((Number) v).longValue();
        } else {
            try {
                return Long.parseLong(v.toString());
            } catch (Throwable var4) {
                return defaultValue;
            }
        }
    }

    public static float anyToFloat(Object v) {
        return anyToFloat(v, 0.0F);
    }

    public static float anyToFloat(Object v, float defaultValue) {
        if (v == null) {
            return defaultValue;
        } else if (v instanceof Float) {
            return (Float) v;
        } else if (v instanceof Number) {
            return ((Number) v).floatValue();
        } else {
            try {
                return Float.parseFloat(v.toString());
            } catch (Throwable var3) {
                return defaultValue;
            }
        }
    }

    public static boolean anyToBool(Object v) {
        return anyToBool(v, false);
    }

    public static boolean anyToBool(Object v, boolean defaultValue) {
        if (v != null && !v.equals("")) {
            if (v instanceof Boolean) {
                return (Boolean) v;
            } else if (v instanceof Number) {
                return ((Number) v).intValue() == 1;
            } else {
                try {
                    String s = v.toString().toLowerCase();
                    if (!s.equals("true") && !s.equals("yes") && !s.equals("t") && !s.equals("y") && !s.equals("1")) {
                        return !s.equals("false") && !s.equals("no") && !s.equals("f") && !s.equals("n") && !s.equals("0") ? defaultValue : false;
                    } else {
                        return true;
                    }
                } catch (Throwable var3) {
                    return defaultValue;
                }
            }
        } else {
            return defaultValue;
        }
    }

    public static double anyToDouble(Object v) {
        return anyToDouble(v, 0.0);
    }

    public static double anyToDouble(Object v, double defaultValue) {
        if (v == null) {
            return defaultValue;
        } else if (v instanceof Double) {
            return (Double) v;
        } else if (v instanceof Number) {
            return ((Number) v).doubleValue();
        } else {
            try {
                return Double.parseDouble(v.toString());
            } catch (Throwable var4) {
                return 0.0;
            }
        }
    }

    public static boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Throwable var2) {
            return false;
        }
    }

    public static BigInteger anyToBigInteger(Object v) {
        if (v == null) {
            return BigInteger.ZERO;
        } else {
            try {
                return new BigInteger(v.toString());
            } catch (Throwable var2) {
                return BigInteger.ZERO;
            }
        }
    }

    public static BigInteger anyToBigInteger(Object v, BigInteger defaultValue) {
        if (v == null) {
            return defaultValue;
        } else {
            try {
                return new BigInteger(v.toString());
            } catch (Throwable var3) {
                return defaultValue;
            }
        }
    }

    public static BigDecimal anyToBigDecimal(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        } else {
            try {
                return new BigDecimal(v.toString());
            } catch (Throwable var2) {
                return BigDecimal.ZERO;
            }
        }
    }

    public static BigDecimal anyToBigDecimal(Object v, BigDecimal defaultValue) {
        if (v == null) {
            return defaultValue;
        } else {
            try {
                return new BigDecimal(v.toString());
            } catch (Throwable var3) {
                return defaultValue;
            }
        }
    }

    public static Map<String, Object> anyToMap(Object v) {
        if (v == null) {
            return null;
        } else {
            return v instanceof Map ? (Map) v : null;
        }
    }

    public static List<Object> anyToList(Object v) {
        if (v == null) {
            return null;
        } else {
            return v instanceof List ? (List) v : null;
        }
    }

    public static Date anyToDate(Object v) {
        if (v == null) {
            return null;
        } else if (v instanceof Date) {
            return (Date) v;
        } else if (v instanceof Number i) {
            return new Date(Long.valueOf(i.toString()));
        } else if (v instanceof String) {
            String s = (String) v;
            if (s.isEmpty()) {
                return null;
            } else if (s.contains("-")) {
                return datetimeStrToDateOrNull(s);
            } else {
                long l = anyToLong(s);
                return l == 0L ? null : new Date(l);
            }
        } else {
            return null;
        }
    }

    public static LocalDateTime anyToLocalDateTime(Object v) {
        if (v == null) {
            return null;
        }

        if (v instanceof LocalDateTime ldt) {
            return ldt; // Early return for direct LocalDateTime instances
        }

        var date = anyToDate(v);
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }

    public static String now() {
        return LocalDateTime.now().format(DATETIME_FM);
    }

    public static boolean validateDateStr(String s) {
        try {
            LocalDate.parse(s, DATE_FM);
            return true;
        } catch (Throwable var2) {
            return false;
        }
    }

    public static boolean validateDatetimeStr(String s) {
        try {
            LocalDateTime.parse(s, DATETIME_FM);
            return true;
        } catch (Throwable var2) {
            return false;
        }
    }

    public static boolean validateDatetimeStrWithSss(String s) {
        try {
            LocalDateTime.parse(s, DATETIMESSS_FM);
            return true;
        } catch (Throwable var2) {
            return false;
        }
    }

    public static long datetimeStrToMillis(String s, DateTimeFormatter fm) {
        LocalDateTime ldt = LocalDateTime.parse(s, fm);
        return ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }

    public static long datetimeStrToMillisOrZero(String s, DateTimeFormatter fm) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(s, fm);
            return ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        } catch (Throwable var3) {
            return 0L;
        }
    }

    public static long datetimeStrToMillis(String s) {
        if (s.length() == 10) {
            s = s + " 00:00:00";
        }

        DateTimeFormatter f = s.length() == 19 ? DATETIME_FM : DATETIMESSS_FM;
        LocalDateTime ldt = LocalDateTime.parse(s, f);
        return ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }

    public static long datetimeStrToMillisOrZero(String s) {
        if (s.length() == 10) {
            s = s + " 00:00:00";
        }

        try {
            DateTimeFormatter f = s.length() == 19 ? DATETIME_FM : DATETIMESSS_FM;
            LocalDateTime ldt = LocalDateTime.parse(s, f);
            return ldt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
        } catch (Throwable var3) {
            return 0L;
        }
    }

    public static Date datetimeStrToDate(String s) {
        long millis = datetimeStrToMillis(s);
        return new Date(millis);
    }

    public static Date datetimeStrToDateOrNull(String s) {
        long millis = datetimeStrToMillisOrZero(s);
        return millis == 0L ? null : new Date(millis);
    }

    public static String millisToDatetimeStr(long millis, DateTimeFormatter fm) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULT_ZONE_ID);
        return ldt.format(fm);
    }

    public static String millisToDatetimeStr(long millis) {
        return millisToDatetimeStr(millis, DATETIME_FM);
    }

    public static String millisToDatetimeStrWithSss(long millis) {
        return millisToDatetimeStr(millis, DATETIMESSS_FM);
    }

    public static String microsToDatetimeStr(long micros) {
        return millisToDatetimeStr(micros / 1000L);
    }

    public static String microsToDatetimeStrWithSss(long micros) {
        return millisToDatetimeStrWithSss(micros / 1000L);
    }

    public static String dateToDatetimeStr(Date dt) {
        return millisToDatetimeStr(dt.getTime());
    }

    public static String dateToDatetimeStrWithSss(Date dt) {
        return millisToDatetimeStrWithSss(dt.getTime());
    }

    public static long localDateToMillis(LocalDate d) {
        LocalTime t = LocalTime.of(0, 0, 0);
        LocalDateTime dt = LocalDateTime.of(d, t);
        return localDatetimeToMillis(dt);
    }

    public static long localDatetimeToMillis(LocalDateTime dt) {
        return dt.atZone(DEFAULT_ZONE_ID).toInstant().toEpochMilli();
    }

    public static Date localDateToDate(LocalDate d) {
        return new Date(localDateToMillis(d));
    }

    public static Date localDatetimeToDate(LocalDateTime dt) {
        return new Date(localDatetimeToMillis(dt));
    }


}
