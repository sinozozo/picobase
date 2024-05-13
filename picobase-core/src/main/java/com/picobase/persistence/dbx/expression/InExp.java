package com.picobase.persistence.dbx.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.picobase.persistence.dbx.DbxUtil.quoteColumnName;

public class InExp<T> implements Expression {

    private final String col;
    private final List<T> values;
    private final boolean not;

    public InExp(String col, List<T> values, boolean not) {
        this.col = col;
        this.values = values;
        this.not = not;
    }

    /**
     * Build converts an expression into a SQL fragment.
     */
    @Override
    public String build(Map<String, Object> params) {
        if (values.isEmpty()) {
            if (this.not) {
                return "";
            }
            return "0=1";
        }

        List<String> values = new ArrayList<>();
        for (Object value : this.values) {
            if (value == null) {
                values.add("NULL");
            } else if (value instanceof Expression subExpression) {
                values.add(subExpression.build(params));
            } else {
                String paramName = "p" + params.size();
                params.put(paramName, value);
                values.add(":" + paramName);
            }
        }

        String col = quoteColumnName(this.col);
        String in = not ? "NOT IN" : "IN";
        if (values.size() == 1) {
            return col + (not ? "<>" : "=") + values.get(0);
        } else {
            return String.format("%s %s (%s)", col, in, String.join(", ", values));
        }
    }
}
