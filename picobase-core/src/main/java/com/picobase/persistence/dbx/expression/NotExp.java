package com.picobase.persistence.dbx.expression;

import cn.hutool.core.util.StrUtil;

import java.util.Map;

// NotExp represents an expression that should prefix "NOT" to a specified expression.
public class NotExp implements Expression {
    private final Expression exp;

    public NotExp(Expression exp) {
        this.exp = exp;
    }

    @Override
    public String build(Map<String, Object> params) {
        var str = this.exp.build(params);
        if (StrUtil.isNotBlank(str)) {
            return "NOT (" + str + ")";
        }
        return "";
    }

}
