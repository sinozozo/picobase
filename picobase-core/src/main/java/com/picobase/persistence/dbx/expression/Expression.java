package com.picobase.persistence.dbx.expression;

import java.util.List;
import java.util.Map;

public interface Expression {

    String build(Map<String, Object> params);

    static Expression newExpr(String e, Map<String, Object>... params) {
        return new Exp(e, params);
    }


    static Expression and(Expression... expr) {
        return new AndOrExp(List.of(expr), "AND");
    }

    static Expression or(Expression... expr) {
        return new AndOrExp(List.of(expr), "OR");
    }

    static Expression enclose(Expression exp) {
        return new EncloseExp(exp);
    }

    /**
     * In generates an IN expression for the specified column and the list of allowed values.
     * If values is empty, a SQL "0=1" will be generated which represents a false expression.
     */
    static Expression in(String col, Integer... values) {
        return new InExp(col, List.of(values), false);
    }

    static Expression in(String col, String... values) {
        return new InExp(col, List.of(values), false);
    }

    static <T> Expression in(String col, List<T> values) {
        return new InExp(col, values, false);
    }

    /**
     * // NotIn generates an NOT IN expression for the specified column and the list of disallowed values.
     * // If values is empty, an empty string will be returned indicating a true expression.
     */
    static Expression notIn(String col, Object... values) {
        return new InExp(col, List.of(values), true);
    }

    static Expression newOp(String op) {
        return new OpExp(op);
    }

    static Expression newHashExpr(Map<String, Object> params) {
        return new HashExp(params);
    }

    static Expression exists(Expression exp) {
        return new ExistsExp(exp, false);
    }

    static Expression notExists(Expression exp) {
        return new ExistsExp(exp, true);
    }

    static Expression not(Expression exp) {
        return new NotExp(exp);
    }

}


// EncloseExp represents a parenthesis enclosed expression.


