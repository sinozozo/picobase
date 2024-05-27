package com.picobase.persistence.dbx;

import cn.hutool.core.util.StrUtil;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.repository.PbRowMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.picobase.persistence.dbx.expression.Expression.and;
import static com.picobase.persistence.dbx.expression.Expression.or;


public class SelectQuery implements Cloneable {
    private List<String> selects;

    private List<String> from;

    private List<String> orderBy;

    private List<String> groupBy;

    private boolean distinct;

    private Map<String, Object> params;

    private PbDbxBuilder builder;

    private String selectOption;

    private List<JoinInfo> join;
    private Expression where;
    private Expression having;
    private long limit;
    private long offset;
    private List<UnionInfo> union;


    public SelectQuery(PbDbxBuilder builder) {
        this.builder = builder;
        this.selects = new ArrayList<>();
        this.from = new ArrayList<>();
        this.orderBy = new ArrayList<>();
        this.groupBy = new ArrayList<>();
        this.params = new HashMap<>();
        this.union = new ArrayList<>();
        this.join = new ArrayList<>();
        this.limit = -1;

    }

    public SelectQuery select(String... cols) {
        this.selects = new ArrayList<>(List.of(cols));
        return this;
    }

    public SelectQuery from(String... tables) {
        this.from.addAll(List.of(tables));
        return this;
    }

    /**
     * // Where specifies the WHERE condition.
     */
    public SelectQuery where(Expression expression) {
        this.where = expression;
        return this;
    }

    public SelectQuery andWhere(Expression expression) {
        this.where = this.where == null ? and(expression) : and(this.where, expression);
        return this;
    }

    public SelectQuery orWhere(Expression expression) {
        this.where = this.where == null ? or(expression) : or(this.where, expression);
        return this;
    }


    public SelectQuery having(Expression expression) {
        this.having = expression;
        return this;
    }

    public SelectQuery andHaving(Expression expression) {
        this.having = and(this.having, expression);
        return this;
    }

    public SelectQuery orHaving(Expression expression) {
        this.having = or(this.having, expression);
        return this;
    }

    public SelectQuery limit(long limit) {
        this.limit = limit;
        return this;
    }

    public SelectQuery offset(long offset) {
        this.offset = offset;
        return this;
    }

    public SelectQuery distinct(boolean v) {
        this.distinct = v;
        return this;
    }

    public SelectQuery union(Query q) {
        this.union.add(new UnionInfo(false, q));
        return this;
    }

    public SelectQuery unionAll(Query q) {
        this.union.add(new UnionInfo(true, q));
        return this;
    }

    public SelectQuery selectOption(String selectOption) {
        this.selectOption = selectOption;
        return this;
    }

    /**
     * // Bind specifies the parameter values to be bound to the query.
     */
    public SelectQuery bind(Map<String, Object> params) {
        this.params = new HashMap<>(params);
        return this;
    }


    /**
     * // AndBind appends additional parameters to be bound to the query.
     */
    public SelectQuery andBind(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    public Query build() {
        Map<String, Object> params = new HashMap<>(this.params); //copy

        QueryBuilder qb = this.builder.QueryBuilder();
        List<String> clauses = List.of(
                qb.buildSelect(this.selects, this.distinct, this.selectOption),
                qb.buildFrom(this.from),
                qb.buildJoin(this.join, params),
                qb.buildWhere(this.where, params),
                qb.buildGroupBy(this.groupBy),
                qb.buildHaving(this.having, params)
        );
        StringBuilder sqlBuf = new StringBuilder();
        clauses.stream().forEach(clause -> {
            if (StrUtil.isNotEmpty(clause)) {
                if (sqlBuf.length() == 0) {
                    sqlBuf.append(clause);
                } else {
                    sqlBuf.append(" ").append(clause);
                }
            }
        });

        String sql = qb.buildOrderByAndLimit(sqlBuf.toString(), this.orderBy, this.limit, this.offset);

        var union = qb.buildUnion(this.union, params);
        if (StrUtil.isNotEmpty(union)) {
            sql = String.format("(%s) %s", sql, union);
        }
        Query query = this.builder.newQuery(sql).bind(params);
        return query;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * // AndOrderBy appends additional columns to the existing ORDER BY clause.
     * // Column names will be properly quoted. A column name can contain "ASC" or "DESC" to indicate its ordering direction.
     */
    public SelectQuery andOrderBy(String... expr) {
        this.orderBy.addAll(List.of(expr));
        return this;
    }

    /**
     * // GroupBy specifies the GROUP BY clause.
     * // Column names will be properly quoted.
     */
    public SelectQuery orderBy(String... expr) {
        this.orderBy = new ArrayList<>(List.of(expr));
        return this;
    }

    public Map<String, Object> row() {
        return this.build().row();
    }

    
    public <T> List<T> all(Class<T> clazz) {
        if (this.from.size() == 0) {
            //TODO 这里通过反射获取表名
            try {
                // 获取方法
                Method method = clazz.getMethod("tableName");
                // 创建对象并调用方法
                T instance = clazz.newInstance();
                String result = (String) method.invoke(instance);
                this.from.add(result);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return this.build().all(clazz);
    }

    public <T> List<T> column(Class<T> clz) {
        return this.build().column(clz);
    }

    public <T> T one(Class<T> clazz) {
        return this.build().one(clazz);
    }


    public <T> T one(PbRowMapper<T> rm) {
        return this.build().one(rm);
    }

    public Long count() {
        return this.build().count();
    }

    public SelectQuery join(String type, String table, Expression on) {
        this.join.add(new JoinInfo(type, table, on));
        return this;
    }

    public SelectQuery innerJoin(String table, Expression on) {
        return this.join("INNER JOIN", table, on);
    }

    public SelectQuery leftJoin(String table, Expression on) {
        return this.join("LEFT JOIN", table, on);
    }

    public SelectQuery rightJoin(String table, Expression on) {
        return this.join("RIGHT JOIN", table, on);
    }

    public SelectQuery groupBy(String... cols) {
        this.groupBy = new ArrayList<>(List.of(cols));
        return this;
    }

    public SelectQuery andGroupBy(String... cols) {
        this.groupBy.addAll(List.of(cols));
        return this;
    }


    public <T> List<T> all(PbRowMapper<T> rm) {
        return this.build().all(rm);
    }

    public List<String> getFrom() {
        return from;
    }
}
