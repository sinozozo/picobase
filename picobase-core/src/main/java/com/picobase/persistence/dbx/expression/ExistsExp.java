package com.picobase.persistence.dbx.expression;

import java.util.Map;

// ExistsExp represents an EXISTS or NOT EXISTS expression.
public class ExistsExp implements Expression {
    private final Expression exp;
    private final boolean not;

    public ExistsExp(Expression exp, boolean not) {
        this.exp = exp;
        this.not = not;
    }

    @Override
    public String build(Map<String, Object> params) {
        var str = this.exp.build(params);
        if (str == null || str.isEmpty()) {
            if (not) {
                return "";
            }
            return "0=1";
        }
        if (not) {
            return "NOT EXISTS (" + str + ")";
        }
        return "EXISTS (" + str + ")";
    }
}
