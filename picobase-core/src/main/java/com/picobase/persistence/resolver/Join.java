package com.picobase.persistence.resolver;


import com.picobase.persistence.dbx.expression.Expression;

public class Join {
    private final String tableName;
    private final String tableAlias;
    private final Expression on;

    public Join(String tableName, String tableAlias, Expression on) {
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.on = on;
    }

    public Join(String tableName, String tableAlias) {
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.on = null;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public Expression getOn() {
        return on;
    }
}
