package com.picobase.util;

public final class StringEscapeUtils {
    private static final String[] REPLACEMENT_CHARS = new String[128];
    private static final String U2028 = "\\u2028";
    private static final String U2029 = "\\u2029";

    public static String escapeJson(String v) {
        if (v == null) {
            return "";
        } else {
            int length = v.length();
            if (length == 0) {
                return v;
            } else {
                int afterReplacement = 0;
                StringBuilder builder = null;

                for (int i = 0; i < length; ++i) {
                    char c = v.charAt(i);
                    String replacement;
                    if (c < 128) {
                        replacement = REPLACEMENT_CHARS[c];
                        if (replacement == null) {
                            continue;
                        }
                    } else if (c == 8232) {
                        replacement = "\\u2028";
                    } else {
                        if (c != 8233) {
                            continue;
                        }

                        replacement = "\\u2029";
                    }

                    if (afterReplacement < i) {
                        if (builder == null) {
                            builder = new StringBuilder(length);
                        }

                        builder.append(v, afterReplacement, i);
                    }

                    if (builder == null) {
                        builder = new StringBuilder(length);
                    }

                    builder.append(replacement);
                    afterReplacement = i + 1;
                }

                if (builder == null) {
                    return v;
                } else {
                    if (afterReplacement < length) {
                        builder.append(v, afterReplacement, length);
                    }

                    return builder.toString();
                }
            }
        }
    }

    private StringEscapeUtils() {
    }

    static {
        for (int i = 0; i <= 31; ++i) {
            REPLACEMENT_CHARS[i] = String.format("\\u%04x", i);
        }

        REPLACEMENT_CHARS[34] = "\\\"";
        REPLACEMENT_CHARS[92] = "\\\\";
        REPLACEMENT_CHARS[9] = "\\t";
        REPLACEMENT_CHARS[8] = "\\b";
        REPLACEMENT_CHARS[10] = "\\n";
        REPLACEMENT_CHARS[13] = "\\r";
        REPLACEMENT_CHARS[12] = "\\f";
    }
}