package com.picobase.persistence.dbx.expression;

import java.util.HashMap;
import java.util.Map;

public class Exp implements Expression {

    private final String e;

    private final Map<String, Object> params;

    public Exp(String e, Map<String, Object>... params) {
        this.e = e;
        if (params.length > 0) {
            this.params = params[0];
        } else {
            this.params = new HashMap<>();
        }
    }

    /**
     * Build converts an expression into a SQL fragment.
     */
    @Override
    public String build(Map<String, Object> rootParams) {
        if (params.isEmpty()) {
            return e; // No parameters to process, return the expression directly
        }
        rootParams.putAll(params); // Add the parameters to the map
        return e;
    }

    public String getE() {
        return e;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}