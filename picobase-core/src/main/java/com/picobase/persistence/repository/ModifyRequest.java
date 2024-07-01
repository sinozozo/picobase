package com.picobase.persistence.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Represents a database UPDATE or INSERT or DELETE statement.
 */

public class ModifyRequest implements Serializable {

    private static final long serialVersionUID = 4548851816596520564L;

    private int executeNo;

    private String sql;

    private boolean rollBackOnUpdateFail = Boolean.FALSE;

    private Object[] args;

    private Map<String, Object> namedArgs;

    public ModifyRequest() {
    }

    public ModifyRequest(String sql) {
        this.sql = sql;
    }

    public int getExecuteNo() {
        return executeNo;
    }

    public ModifyRequest setExecuteNo(int executeNo) {
        this.executeNo = executeNo;
        return this;
    }

    public String getSql() {
        return sql;
    }

    public ModifyRequest setSql(String sql) {
        this.sql = sql;
        return this;
    }

    public boolean isRollBackOnUpdateFail() {
        return rollBackOnUpdateFail;
    }

    public ModifyRequest setRollBackOnUpdateFail(boolean rollBackOnUpdateFail) {
        this.rollBackOnUpdateFail = rollBackOnUpdateFail;
        return this;
    }

    public Object[] getArgs() {
        return args;
    }

    public ModifyRequest setArgs(Object[] args) {
        this.args = args;
        return this;
    }

    public Map<String, Object> getNamedArgs() {
        return namedArgs;
    }

    public ModifyRequest setNamedArgs(Map<String, Object> namedArgs) {
        this.namedArgs = namedArgs;
        return this;
    }

    @Override
    public String toString() {
        return "SQL{" + "executeNo=" + executeNo + ", sql='" + sql + '\'' + ", args=" + Arrays.toString(args) + '}';
    }
}
