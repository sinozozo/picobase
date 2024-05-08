
package com.picobase.persistence.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a database UPDATE or INSERT or DELETE statement.
 *
 */

public class ModifyRequest implements Serializable {
    
    private static final long serialVersionUID = 4548851816596520564L;
    
    private int executeNo;
    
    private String sql;
    
    private boolean rollBackOnUpdateFail = Boolean.FALSE;
    
    private Object[] args;

    private Map<String,Object> namedArgs;
    
    public ModifyRequest() {
    }
    
    public ModifyRequest(String sql) {
        this.sql = sql;
    }
    
    public int getExecuteNo() {
        return executeNo;
    }
    
    public void setExecuteNo(int executeNo) {
        this.executeNo = executeNo;
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public Object[] getArgs() {
        return args;
    }
    
    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Map<String, Object> getNamedArgs() {
        return namedArgs;
    }

    public void setNamedArgs(Map<String, Object> namedArgs) {
        this.namedArgs = namedArgs;
    }

    
    public boolean isRollBackOnUpdateFail() {
        return rollBackOnUpdateFail;
    }
    
    public void setRollBackOnUpdateFail(boolean rollBackOnUpdateFail) {
        this.rollBackOnUpdateFail = rollBackOnUpdateFail;
    }
    
    @Override
    public String toString() {
        return "SQL{" + "executeNo=" + executeNo + ", sql='" + sql + '\'' + ", args=" + Arrays.toString(args) + '}';
    }
}
