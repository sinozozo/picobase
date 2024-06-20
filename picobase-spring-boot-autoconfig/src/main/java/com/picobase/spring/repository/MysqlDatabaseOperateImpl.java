package com.picobase.spring.repository;

import cn.hutool.core.util.ClassUtil;
import com.picobase.persistence.repository.ModifyRequest;
import com.picobase.persistence.repository.PbRowMapper;
import com.picobase.persistence.repository.PbRowMapperRegistry;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Mysql operation
 */
public class MysqlDatabaseOperateImpl implements BaseDatabaseOperate {


    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private TransactionTemplate transactionTemplate;


    public MysqlDatabaseOperateImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <R> R queryOne(String sql, Class<R> cls) {
        return queryOne(sql, new Object[0], cls);
    }

    @Override
    public <R> R queryOne(String sql, Object[] args, Class<R> cls) {
        if (ClassUtil.isBasicType(cls)) {
            return queryOne(jdbcTemplate, sql, args, cls);
        }

        if (Map.class.isAssignableFrom(cls)) {
            return queryOne(jdbcTemplate, sql, args, (rs, rowNum) -> (R) new ColumnMapRowMapper().mapRow(rs, rowNum));
        }

        return (R) queryOne(jdbcTemplate, sql, args, PbRowMapperRegistry.getInstance().getPbRowMapper(cls, true));
    }

    @Override
    public <R> R queryOne(String sql, Map<String, Object> args, Class<R> cls) {
        if (ClassUtil.isBasicType(cls)) {
            return queryOne(namedParameterJdbcTemplate, sql, args, cls);
        }

        if (Map.class.isAssignableFrom(cls)) {
            return queryOne(namedParameterJdbcTemplate, sql, args, (rs, rowNum) -> (R) new ColumnMapRowMapper().mapRow(rs, rowNum));
        }

        return (R) queryOne(namedParameterJdbcTemplate, sql, args, PbRowMapperRegistry.getInstance().getPbRowMapper(cls, true));
    }

    @Override
    public <R> R queryOne(String sql, Object[] args, PbRowMapper<R> mapper) {
        return queryOne(jdbcTemplate, sql, args, mapper);
    }

    @Override
    public <R> R queryOne(String sql, Map<String, Object> args, PbRowMapper<R> mapper) {
        return queryOne(namedParameterJdbcTemplate, sql, args, mapper);
    }

    @Override
    public <R> List<R> queryMany(String sql, Object[] args, PbRowMapper<R> mapper) {
        return queryMany(jdbcTemplate, sql, args, mapper);
    }

    @Override
    public <R> List<R> queryMany(String sql, Map<String, Object> args, PbRowMapper<R> mapper) {
        return queryMany(namedParameterJdbcTemplate, sql, args, mapper);
    }

    @Override
    public <R> List<R> queryMany(String sql, Object[] args, Class<R> rClass) {
        if (ClassUtil.isBasicType(rClass)) {
            return queryMany(jdbcTemplate, sql, args, rClass);
        }

        if (Map.class.isAssignableFrom(rClass)) {
            return queryMany(jdbcTemplate, sql, args, (rs, rowNum) -> (R) new ColumnMapRowMapper().mapRow(rs, rowNum));
        }

        return queryMany(jdbcTemplate, sql, args, PbRowMapperRegistry.getInstance().getPbRowMapper(rClass, true));
    }

    @Override
    public <R> List<R> queryMany(String sql, Map<String, Object> args, Class<R> rClass) {
        if (ClassUtil.isBasicType(rClass)) {
            return queryMany(namedParameterJdbcTemplate, sql, args, rClass);
        }

        if (Map.class.isAssignableFrom(rClass)) {
            return queryMany(namedParameterJdbcTemplate, sql, args, (rs, rowNum) -> (R) new ColumnMapRowMapper().mapRow(rs, rowNum));
        }

        return queryMany(namedParameterJdbcTemplate, sql, args, PbRowMapperRegistry.getInstance().getPbRowMapper(rClass, true));
    }

    @Override
    public List<Map<String, Object>> queryMany(String sql, Object[] args) {
        return queryMany(jdbcTemplate, sql, args);
    }

    @Override
    public List<Map<String, Object>> queryMany(String sql, Map<String, Object> args) {
        return queryMany(namedParameterJdbcTemplate, sql, args);
    }


    @Override
    public Boolean update(List<ModifyRequest> modifyRequests, BiConsumer<Integer, Throwable> consumer) {
        return update(transactionTemplate, jdbcTemplate, namedParameterJdbcTemplate, modifyRequests, consumer);
    }

    @Override
    public Object runInTransaction(Function<Object, Object> action, boolean rollBack) {
        return runInTransaction(transactionTemplate, action, rollBack);

    }

    @Override
    public Boolean update(List<ModifyRequest> requestList) {
        return update(transactionTemplate, jdbcTemplate, namedParameterJdbcTemplate, requestList);
    }
}
