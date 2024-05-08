
package com.picobase.persistence.model;

import java.util.List;
import java.util.Map;

/**
 * The object returned by the execution of the PbMapper method.
 *
 **/

public class MapperResult {
    
    public MapperResult() { }
    
    public MapperResult(String sql, List<Object> paramList) {
        this.sql = sql;
        this.paramList = paramList;
    }

    public MapperResult(String sql, Map<String,Object> paramMap) {
        this.sql = sql;
        this.paramMap = paramMap;
    }
    
    private String sql;
    
    private List<Object> paramList;

    private Map<String,Object> paramMap;
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public List<Object> getParamList() {
        return paramList;
    }


    
    public void setParamList(List<Object> paramList) {
        this.paramList = paramList;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "MapperResult{" +
                "sql='" + sql + '\'' +
                ", paramList=" + paramList +
                ", paramMap=" + paramMap +
                '}';
    }
}
