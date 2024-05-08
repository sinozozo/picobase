package com.picobase.persistence.dbx;

import cn.hutool.core.util.StrUtil;

import java.util.*;

import static com.picobase.persistence.dbx.DbxUtil.quoteColumnName;

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

class Exp implements Expression {

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
}


class AndOrExp implements Expression {
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

class OpExp implements Expression {
    private final String op;

    public OpExp(String op) {
        this.op = op;
    }

    @Override
    public String build(Map<String, Object> params) {
        return op;
    }
}

class HashExp extends HashMap<String, Object> implements Expression {


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

class InExp<T> implements Expression {

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

// EncloseExp represents a parenthesis enclosed expression.

class EncloseExp implements Expression {
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

// ExistsExp represents an EXISTS or NOT EXISTS expression.
class ExistsExp implements Expression {
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


// NotExp represents an expression that should prefix "NOT" to a specified expression.
class NotExp implements Expression {
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
