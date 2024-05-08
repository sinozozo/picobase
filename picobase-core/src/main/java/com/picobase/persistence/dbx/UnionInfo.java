package com.picobase.persistence.dbx;


public class UnionInfo {
    private final boolean all;
    private final Query query;

    public UnionInfo(boolean all, Query query) {
        this.all = all;
        this.query = query;
    }

    public boolean isAll() {
        return all;
    }

    public Query getQuery() {
        return query;
    }
}
