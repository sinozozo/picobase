package com.picobase.search;


import com.picobase.persistence.resolver.FieldResolver;

import java.util.ArrayList;
import java.util.List;

public class SortField {

    public static final String SortAsc = "ASC";
    public static final String SortDesc = "DESC";
    private static final String RandomSortKey = "@random"; // mysql 中需要使用 RAND()函数

    private String name;
    private String direction;

    public SortField(String name, String direction) {
        this.name = name;
        this.direction = direction;
    }


    /**
     * // ParseSortFromString parses the provided string expression
     * // into a slice of SortFields.
     *
     * @param str
     * @return
     */
    public static List<SortField> parseSortFromString(String str) {
        List<SortField> fields = new ArrayList<>();
        String[] data = str.split(",", -1);

        for (String field : data) {
            // trim whitespaces
            field = field.trim();
            if (field.startsWith("-")) {
                fields.add(new SortField(field.substring(1), SortDesc));
            } else {
                if (field.startsWith("+")) {
                    field = field.substring(1);
                }
                fields.add(new SortField(field, SortAsc));
            }
        }

        return fields;
    }

    // BuildExpr resolves the sort field into a valid db sort expression.
    public String buildExpr(FieldResolver fieldResolver) {
        // special case for random sort
        if (name.equals(RandomSortKey)) {
            return "RAND()";
        }

        var result = fieldResolver.resolve(name);

        // invalidate empty fields and non-column identifiers
        if (result == null || result.getParams().size() > 0 || result.getIdentifier() == null || result.getIdentifier().isEmpty() || result.getIdentifier().toLowerCase().equals("null")) {
            throw new IllegalArgumentException("invalid sort field :" + name);
        }

        return String.format("%s %s", result.getIdentifier(), direction);
    }

}
