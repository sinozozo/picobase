package com.picobase.persistence.dbx;

import cn.hutool.core.util.StrUtil;
import com.picobase.persistence.dbx.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * // concatExpr defines an expression that concatenates multiple
 * // other expressions with a specified separator.
 */
public class ConcatExpr implements Expression {

    private String separator;
    private List<Expression> parts = new ArrayList<>();


    /**
     * @param params // Params represents a list of parameter values to be bound to a SQL statement.
     *               // The map keys are the parameter names while the map values are the corresponding parameter values.
     * @return
     */
    @Override
    public String build(Map<String, Object> params) {
        if (parts.size() == 0) {
            return "";
        }
        List<String> stringParts = new ArrayList<>();

        for (Expression p : parts) {
            if (p == null) {
                continue;
            }
            String sql = p.build(params);
            if (!"".equals(sql)) {
                stringParts.add(sql);
            }
        }

        // skip extra parenthesis for single concat expression
        if (stringParts.size() == 1
                && !stringParts.get(0).toUpperCase().contains(" AND ")
                && !stringParts.get(0).toUpperCase().contains(" OR ")) {
            return stringParts.get(0);
        }
        return "(" + StrUtil.join(separator, stringParts) + ")";
    }

    public void addPart(Expression expr) {
        this.parts.add(expr);
    }

    public String getSeparator() {
        return separator;
    }

    public ConcatExpr setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public List<Expression> getParts() {
        return parts;
    }

    public ConcatExpr setParts(List<Expression> parts) {
        this.parts = parts;
        return this;
    }
}