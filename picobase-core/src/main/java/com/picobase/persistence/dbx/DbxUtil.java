package com.picobase.persistence.dbx;

import java.util.regex.Pattern;


import static com.picobase.util.PbConstants.*;

public class DbxUtil {

    private static final Pattern COLUMNIFY_REMOVE_REGEX = Pattern.compile("[^\\w\\.\\*\\-\\_\\@\\#]+");
    private static final String SNAKECASE_SPLIT_REGEX = "[\\W_]+";


    /**
     * 方法用于去除字符串中不适合作为数据库标识符的字符。
     * <p>
     * 具体来说，该方法会将以下字符替换为空字符串:
     * <p>
     * 所有非字母数字、点号、星号、减号、下划线、at 符号和井号的字符。
     */
    public static String columnify(String str) {
        return COLUMNIFY_REMOVE_REGEX.matcher(str).replaceAll("");
    }

    public static void main(String[] args) {
        String input = "This is a sample string with in-valid characters!@#$%^&*()_+";
        String result = columnify(input);
        System.out.println(result); // Output: Thisisasamplestringwithinvalidcharacters
    }


    /**
     * Removes all non-word characters and converts any English text into a snake case.
     * "ABBREVIATIONS" are preserved, e.g., "myTestDB" will become "my_test_db".
     *
     * @param str the input string
     * @return the snake case string
     */
    public static String snakeCase(String str) {
        StringBuilder result = new StringBuilder();
        String[] words = str.split(SNAKECASE_SPLIT_REGEX, -1);

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append("_");
            }

            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (Character.isUpperCase(c) && i > 0 && !Character.isUpperCase(word.charAt(i - 1))) {
                    result.append("_");
                }
                result.append(c);
            }
        }

        return result.toString().toLowerCase();
    }


    public static String quoteColumnName(String s) {
        if (s.contains("(") || s.contains("{{") || s.contains("[[")) {
            return s;
        }

        int dotIndex = s.lastIndexOf('.');
        String prefix = "";
        if (dotIndex != -1) {
            prefix = quoteTableName(s.substring(0, dotIndex)) + ".";
            s = s.substring(dotIndex + 1);
        }

        return prefix + quoteSimpleColumnName(s);
    }

    public static String quoteSimpleColumnName(String s) {
        if (s.contains("`") || s.equals("*")) {
            return s;
        }
        return "`" + s + "`";
    }

    public static String quoteTableName(String s) {
        if (s.contains("(") || s.contains("{{") || s.contains("[")) { // Added handling for square brackets
            return s;
        }

        if (!s.contains(".")) {
            return quoteSimpleTableName(s);
        }

        String[] parts = s.split("\\.", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = quoteSimpleTableName(parts[i]);
        }

        return String.join(".", parts);
    }

    public static String quoteSimpleTableName(String s) {
        if (s.contains("`")) {
            return s;
        }
        return "`" + s + "`";
    }

    public static String[] splitModifier(String combined) {
        var parts = combined.split(":", -1);
        if (parts.length != 2) {
            return new String[]{combined, ""};
        }
        switch (parts[1]) {
            case EachModifier, IssetModifier, LengthModifier:
                return new String[]{parts[0], parts[1]};
            default:
                throw new RuntimeException("unknown modifier in " + combined);
        }
    }




}
