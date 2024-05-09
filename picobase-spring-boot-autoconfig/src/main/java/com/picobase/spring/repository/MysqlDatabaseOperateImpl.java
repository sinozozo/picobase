package com.picobase.spring.repository;

import com.picobase.model.Model;
import com.picobase.persistence.mapper.PbMapper;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.ModifyRequest;
import com.picobase.persistence.repository.PbRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Mysql operation
 */
public class MysqlDatabaseOperateImpl implements BaseDatabaseOperate {


    private JdbcTemplate jdbcTemplate;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private TransactionTemplate transactionTemplate;

    private PbMapperManager mapperManager;

    public MysqlDatabaseOperateImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate, PbMapperManager mapperManager) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.mapperManager = mapperManager;
    }

    @Override
    public <R> R queryOne(String sql, Class<R> cls) {
        if (Model.class.isAssignableFrom(cls)) {
            PbMapper mapper = mapperManager.findMapper(cls);
            return (R) queryOne(jdbcTemplate, sql, mapper.getPbRowMapper());
        }

        return queryOne(jdbcTemplate, sql, cls);
    }

    @Override
    public <R> R queryOne(String sql, Object[] args, Class<R> cls) {
        if (Model.class.isAssignableFrom(cls)) {
            PbMapper mapper = mapperManager.findMapper(cls);
            return (R) queryOne(jdbcTemplate, sql, args, mapper.getPbRowMapper());
        }

        return queryOne(jdbcTemplate, sql, args, cls);
    }

    @Override
    public <R> R queryOne(String sql, Map<String, Object> args, Class<R> cls) {
        if (Model.class.isAssignableFrom(cls)) {
            PbMapper mapper = mapperManager.findMapper(cls);
            return (R) queryOne(namedParameterJdbcTemplate, sql, args, mapper.getPbRowMapper());
        }

        return queryOne(namedParameterJdbcTemplate, sql, args, cls);
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
        if (Model.class.isAssignableFrom(rClass)) {
            PbMapper mapper = mapperManager.findMapper(rClass);
            return queryMany(jdbcTemplate, sql, args, mapper.getPbRowMapper());
        }

        return queryMany(jdbcTemplate, sql, args, rClass);
    }

    @Override
    public <R> List<R> queryMany(String sql, Map<String, Object> args, Class<R> rClass) {
        if (Model.class.isAssignableFrom(rClass)) {
            PbMapper mapper = mapperManager.findMapper(rClass);
            return queryMany(namedParameterJdbcTemplate, sql, args, mapper.getPbRowMapper());
        }

        return queryMany(namedParameterJdbcTemplate, sql, args, rClass);
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
    public Boolean update(List<ModifyRequest> requestList) {
        return update(transactionTemplate, jdbcTemplate, namedParameterJdbcTemplate, requestList);
    }
}
