package com.picobase.persistence.dbx;


public class JoinInfo {
    private final String join;
    private final String table;
    private final Expression on;

    public JoinInfo(String join, String table, Expression on) {
        this.join = join;
        this.table = table;
        this.on = on;
    }

    public String getJoin() {
        return join;
    }

    public String getTable() {
        return table;
    }

    public Expression getOn() {
        return on;
    }
}
