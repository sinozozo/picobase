package com.picobase.persistence.dbx.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AndOrExp implements Expression {
    private final List<Expression> exps;
    private final String op;

    public AndOrExp(List<Expression> exps, String op) {
        this.exps = exps;
        this.op = op;
    }

    @Override
    public String build(Map<String, Object> params) {
        if (exps.isEmpty()) {
            return "";
        }

        List<String> parts = new ArrayList<>();
        for (Expression exp : exps) {
            if (exp == null) {
                continue;
            }
            String sql = exp.build(params);
            if (!sql.isEmpty()) {
                parts.add(sql);
            }
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return "(" + String.join(") " + this.op + " (", parts) + ")";
    }
}