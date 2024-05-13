package com.picobase.persistence.dbx.expression;

import java.util.Map;

public class OpExp implements Expression {
    private final String op;

    public OpExp(String op) {
        this.op = op;
    }

    @Override
    public String build(Map<String, Object> params) {
        return op;
    }
}
