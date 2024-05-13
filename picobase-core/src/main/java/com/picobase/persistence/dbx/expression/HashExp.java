package com.picobase.persistence.dbx.expression;

import cn.hutool.core.util.StrUtil;

import java.util.*;

import static com.picobase.persistence.dbx.DbxUtil.quoteColumnName;

public class HashExp extends HashMap<String, Object> implements Expression {


    public HashExp(Map<String, Object> params) {
        super(params);
    }

    /**
     * Build converts an expression into a SQL fragment.
     */
    @Override
    public String build(Map<String, Object> params) {
        if (this.isEmpty()) {
            return "";
        }

        // Sort key names for consistency
        List<String> names = new ArrayList<>(this.keySet());
        names.sort(Comparator.naturalOrder());

        List<String> parts = new ArrayList<>();
        for (String name : names) {
            Object value = this.get(name);
            String sql;
            if (value == null) {
                sql = quoteColumnName(name) + " IS NULL";
            } else if (value instanceof Expression subExpression) {
                sql = subExpression.build(params);
                if (StrUtil.isNotEmpty(sql)) {
                    sql = "(" + subExpression.build(params) + ")";
                }
            } else if (value instanceof List values) {
                sql = Expression.in(name, values).build(params);

            } else {
                String paramName = "p" + params.size();
                sql = quoteColumnName(name) + " = :" + paramName;
                params.put(paramName, value);
            }
            if (!sql.isEmpty()) {
                parts.add(sql);
            }
        }

        if (parts.size() == 1) {
            return parts.get(0);
        }
        return String.join(" AND ", parts);
    }
}