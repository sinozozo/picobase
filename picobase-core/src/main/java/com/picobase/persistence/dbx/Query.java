package com.picobase.persistence.dbx;


import com.picobase.exception.PbException;
import com.picobase.persistence.repository.ModifyRequest;
import com.picobase.persistence.repository.PbDatabaseOperate;
import com.picobase.persistence.repository.PbRowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class Query {
    private Map<String, Object> params;
    private String sql;
    private String rawSql;
    private PbDatabaseOperate dbOperate;


    public Query(PbDatabaseOperate dbOperate, String sql) {
        this.params = new HashMap<>();
        this.sql = sql;
        this.dbOperate = dbOperate;
    }

    public Query bind(Map<String, Object> params) {
        if (this.params == null) {
            this.params = params;
        } else {
            this.params.putAll(params);
        }
        return this;
    }

    /**
     * 返回一行数据 ， Map结构
     *
     * @return
     */
    public Map<String, Object> row() {
        return this.dbOperate.queryOne(this.sql, this.params, Map.class);
    }

    /**
     * 返回所有数据
     *
     * @param clz List中包含的数据类型
     * @param <T> List中包含的数据类型
     * @return 所有数据
     */
    public <T> List<T> all(Class<T> clz) {
        return this.dbOperate.queryMany(this.sql, this.params, clz);
    }

    public <T> List<T> column(Class<T> clz) {
        return this.dbOperate.queryMany(this.sql, this.params, clz);
    }

    public Long count() {
        return this.dbOperate.queryOne(this.sql, this.params, Long.class);
    }

    public <T> T one(Class<T> clz) {
        return this.dbOperate.queryOne(this.sql, this.params, clz);
    }

    public <T> T one(PbRowMapper<T> rm) {
        return this.dbOperate.queryOne(this.sql, this.params, rm);
    }

    public String sql() {
        return this.sql;
    }

    public Integer execute() {
        ModifyRequest mr = new ModifyRequest();
        mr.setExecuteNo(1);
        mr.setSql(this.sql);
        mr.setNamedArgs(this.params);
        mr.setRollBackOnUpdateFail(true);
        AtomicReference<Integer> i = new AtomicReference<>(0);
        this.dbOperate.update(List.of(mr), (rowNum, e) -> {
            if (e != null) {
                throw new PbException(e);
            }
            i.set(rowNum);

        });
        return i.get();
    }

    public <T> List<T> all(PbRowMapper<T> rm) {
        return this.dbOperate.queryMany(this.sql, this.params, rm);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getRawSql() {
        return rawSql;
    }

    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }

    public PbDatabaseOperate getDbOperate() {
        return dbOperate;
    }

    public void setDbOperate(PbDatabaseOperate dbOperate) {
        this.dbOperate = dbOperate;
    }
}
