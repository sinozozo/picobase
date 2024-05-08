
package com.picobase.persistence.mapper;

import cn.hutool.core.collection.CollUtil;
import com.picobase.persistence.repository.PbRowMapper;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The abstract mapper contains CRUD methods.
 *
 **/

public abstract class AbstractMapper<T> implements PbMapper {

 /*   private final Class<T> clazz;
    public AbstractMapper(Class<T> clazz) {
        this.clazz = clazz;
       *//* ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.clazz = (Class<T>) genericSuperclass.getActualTypeArguments()[0];*//*
    }

    public Class<T> getModelClass(){
        return this.clazz;
    }*/

    public  abstract PbRowMapper<T> getPbRowMapper() ;
    
    @Override
    public String select(List<String> columns, List<String> where) {
        StringBuilder sql = new StringBuilder();
        String method = "SELECT ";
        sql.append(method);
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            if (i == columns.size() - 1) {
                sql.append(" ");
            } else {
                sql.append(",");
            }
        }
        sql.append("FROM ");
        sql.append(getTableName());
        sql.append(" ");
        
        if (CollUtil.isEmpty(where)) {
            return sql.toString();
        }
        
        appendWhereClause(where, sql);
        return sql.toString();
    }
    
    @Override
    public String insert(List<String> columns) {
        StringBuilder sql = new StringBuilder();
        String method = "INSERT INTO ";
        sql.append(method);
        sql.append(getTableName());
        
        int size = columns.size();
        sql.append("(");
        for (int i = 0; i < size; i++) {
            sql.append(columns.get(i));
            if (i != columns.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(") ");
        
        sql.append("VALUES");
        sql.append("(");
        for (int i = 0; i < size; i++) {
            sql.append("?");
            if (i != columns.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");
        return sql.toString();
    }
    
    @Override
    public String update(List<String> columns, List<String> where) {
        StringBuilder sql = new StringBuilder();
        String method = "UPDATE ";
        sql.append(method);
        sql.append(getTableName()).append(" ").append("SET ");
        
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i)).append(" = ").append("?");
            if (i != columns.size() - 1) {
                sql.append(",");
            }
        }
        
        if (CollUtil.isEmpty(where)) {
            return sql.toString();
        }
        
        sql.append(" ");
        appendWhereClause(where, sql);
        
        return sql.toString();
    }
    
    @Override
    public String delete(List<String> params) {
        StringBuilder sql = new StringBuilder();
        String method = "DELETE ";
        sql.append(method).append("FROM ").append(getTableName()).append(" ").append("WHERE ");
        for (int i = 0; i < params.size(); i++) {
            sql.append(params.get(i)).append(" ").append("=").append(" ? ");
            if (i != params.size() - 1) {
                sql.append("AND ");
            }
        }
        
        return sql.toString();
    }
    
    @Override
    public String count(List<String> where) {
        StringBuilder sql = new StringBuilder();
        String method = "SELECT ";
        sql.append(method);
        sql.append("COUNT(*) FROM ");
        sql.append(getTableName());
        sql.append(" ");
        
        if (null == where || where.size() == 0) {
            return sql.toString();
        }
        
        appendWhereClause(where, sql);
        
        return sql.toString();
    }
    
    @Override
    public String[] getPrimaryKeyGeneratedKeys() {
        return new String[] {"id"};
    }
    
    private void appendWhereClause(List<String> where, StringBuilder sql) {
        sql.append("WHERE ");
        for (int i = 0; i < where.size(); i++) {
            sql.append(where.get(i)).append(" = ").append("?");
            if (i != where.size() - 1) {
                sql.append(" AND ");
            }
        }
    }


}
