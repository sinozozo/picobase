package com.picobase.persistence.resolver;

import com.picobase.persistence.dbx.DbxUtil;
import com.picobase.persistence.dbx.expression.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiMatchSubquery implements Expression {
    private String baseTableAlias;
    private String fromTableName;
    private String fromTableAlias;
    private String valueIdentifier;
    private List<Join> joins;
    private Map<String, Object> params;

    public MultiMatchSubquery(String activeTableAlias) {
        this.baseTableAlias = activeTableAlias;
        this.params = new HashMap<>();
        this.joins = new ArrayList<>();
    }

    /**
     * Build converts the expression into a SQL fragment.
     * <p>
     * Implements [Expression] interface.
     */
    @Override
    public String build(Map<String, Object> rootParams) {
        if ("".equals(this.baseTableAlias) || "".equals(this.fromTableName) || "".equals(this.fromTableAlias)) {
            return "0=1";
        }

        if (rootParams == null) {
            rootParams = this.params;
        } else {
            rootParams.putAll(this.params);
        }
        StringBuilder mergedJoins = new StringBuilder();
        for (int i = 0; i < this.joins.size(); i++) {
            var j = this.joins.get(i);
            if (i > 0) {
                mergedJoins.append(" ");
            }
            mergedJoins.append("LEFT JOIN ")
                    .append(DbxUtil.quoteTableName(j.getTableName()))
                    .append(" ")
                    .append(DbxUtil.quoteTableName(j.getTableAlias()));
            if (j.getOn() != null) {
                mergedJoins.append(" ON ").append(j.getOn().build(rootParams));
            }
        }

        return String.format("SELECT %s as `multiMatchValue` FROM %s %s %s WHERE %s = %s",
                DbxUtil.quoteColumnName(this.valueIdentifier),
                DbxUtil.quoteColumnName(this.fromTableName),
                DbxUtil.quoteColumnName(this.fromTableAlias),
                mergedJoins,
                DbxUtil.quoteColumnName(this.fromTableAlias + ".id"),
                DbxUtil.quoteColumnName(this.baseTableAlias + ".id")
        );
    }

    public String getBaseTableAlias() {
        return baseTableAlias;
    }

    public MultiMatchSubquery setBaseTableAlias(String baseTableAlias) {
        this.baseTableAlias = baseTableAlias;
        return this;
    }

    public String getFromTableName() {
        return fromTableName;
    }

    public MultiMatchSubquery setFromTableName(String fromTableName) {
        this.fromTableName = fromTableName;
        return this;
    }

    public String getFromTableAlias() {
        return fromTableAlias;
    }

    public MultiMatchSubquery setFromTableAlias(String fromTableAlias) {
        this.fromTableAlias = fromTableAlias;
        return this;
    }

    public String getValueIdentifier() {
        return valueIdentifier;
    }

    public MultiMatchSubquery setValueIdentifier(String valueIdentifier) {
        this.valueIdentifier = valueIdentifier;
        return this;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public MultiMatchSubquery setJoins(List<Join> joins) {
        this.joins = joins;
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public MultiMatchSubquery setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }
}