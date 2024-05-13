package com.picobase.persistence.dbx.expression;

import java.util.Map;

public class EncloseExp implements Expression {
    private final Expression exp;

    public EncloseExp(Expression exp) {
        this.exp = exp;
    }

    @Override
    public String build(Map<String, Object> params) {
        var str = this.exp.build(params);
        if (str == null || str.isEmpty()) {
            return "";
        }
        return "(" + str + ")";
    }
}