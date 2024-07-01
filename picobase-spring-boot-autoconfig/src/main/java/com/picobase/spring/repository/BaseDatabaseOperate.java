package com.picobase.spring.repository;

import com.picobase.PbManager;
import com.picobase.log.PbLog;
import com.picobase.persistence.repository.ModifyRequest;
import com.picobase.persistence.repository.PbDatabaseOperate;
import com.picobase.persistence.repository.PbRowMapper;
import com.picobase.util.ExceptionHelper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The Mysql database basic operation.
 */
public interface BaseDatabaseOperate extends PbDatabaseOperate {

    PbLog LOGGER = PbManager.getLog();

    /**
     * query one result by sql then convert result to target type.
     *
     * @param jdbcTemplate {@link JdbcTemplate}
     * @param sql          sql
     * @param cls          target type
     * @param <R>          target type
     * @return R
     */
    default <R> R queryOne(JdbcTemplate jdbcTemplate, String sql, Class<R> cls) {
        try {
            return jdbcTemplate.queryForObject(sql, cls);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("can't get connection : {}", ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("DataAccessException : {}", ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    default <R> R queryOne(JdbcTemplate jdbcTemplate, String sql, PbRowMapper<R> mapper) {
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> (R) mapper.mapRow(rs, rowNum));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("can't get connection : {}", ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("DataAccessException : {}", ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    /**
     * query one result by sql and args then convert result to target type.
     *
     * @param jdbcTemplate {@link JdbcTemplate}
     * @param sql          sql
     * @param args         args
     * @param cls          target type
     * @param <R>          target type
     * @return R
     */
    default <R> R queryOne(JdbcTemplate jdbcTemplate, String sql, Object[] args, Class<R> cls) {
        try {
            return jdbcTemplate.queryForObject(sql, args, cls);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }


    default <R> R queryOne(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, Object> args, Class<R> cls) {
        try {
            return jdbcTemplate.queryForObject(sql, args, cls);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    /**
     * query one result by sql and args then convert result to target type through {@link RowMapper}.
     *
     * @param jdbcTemplate {@link JdbcTemplate}
     * @param sql          sql
     * @param args         args
     * @param mapper       {@link RowMapper}
     * @param <R>          target type
     * @return R
     */
    default <R> R queryOne(JdbcTemplate jdbcTemplate, String sql, Object[] args, PbRowMapper<R> mapper) {
        try {
            return jdbcTemplate.queryForObject(sql, args, (rs, rowNum) -> (R) mapper.mapRow(rs, rowNum));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    default <R> R queryOne(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, Object> args, PbRowMapper<R> mapper) {
        try {
            return jdbcTemplate.queryForObject(sql, args, (rs, rowNum) -> (R) mapper.mapRow(rs, rowNum));
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    /**
     * query many result by sql and args then convert result to target type through {@link RowMapper}.
     *
     * @param jdbcTemplate {@link JdbcTemplate}
     * @param sql          sql
     * @param args         args
     * @param mapper       {@link RowMapper}
     * @param <R>          target type
     * @return result list
     */
    default <R> List<R> queryMany(JdbcTemplate jdbcTemplate, String sql, Object[] args, PbRowMapper<R> mapper) {
        try {
            return jdbcTemplate.query(sql, args, (rs, rowNum) -> (R) mapper.mapRow(rs, rowNum));
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    default <R> List<R> queryMany(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, Object> args, PbRowMapper<R> mapper) {
        try {
            return jdbcTemplate.query(sql, args, (rs, rowNum) -> (R) mapper.mapRow(rs, rowNum));
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    /**
     * query many result by sql and args then convert result to target type.
     *
     * @param jdbcTemplate {@link JdbcTemplate}
     * @param sql          sql
     * @param args         args
     * @param rClass       target type class
     * @param <R>          target type
     * @return result list
     */
    default <R> List<R> queryMany(JdbcTemplate jdbcTemplate, String sql, Object[] args, Class<R> rClass) {
        try {
            return jdbcTemplate.queryForList(sql, args, rClass);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    default <R> List<R> queryMany(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, Object> args, Class<R> rClass) {
        try {
            return jdbcTemplate.queryForList(sql, args, rClass);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    /**
     * query many result by sql and args then convert result to List&lt;Map&lt;String, Object&gt;&gt;.
     *
     * @param jdbcTemplate {@link JdbcTemplate}
     * @param sql          sql
     * @param args         args
     * @return List&lt;Map&lt;String, Object&gt;&gt;
     */
    default List<Map<String, Object>> queryMany(JdbcTemplate jdbcTemplate, String sql, Object[] args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    default List<Map<String, Object>> queryMany(NamedParameterJdbcTemplate jdbcTemplate, String sql, Map<String, Object> args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] {}", e.toString());
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", sql, args,
                    ExceptionHelper.getAllExceptionMsg(e));
            throw e;
        }
    }

    /**
     * execute update operation.
     *
     * @param transactionTemplate {@link TransactionTemplate}
     * @param jdbcTemplate        {@link JdbcTemplate}
     * @param contexts            {@link List} ModifyRequest list
     * @return {@link Boolean}
     */
    default Boolean update(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                           List<ModifyRequest> contexts) {
        return update(transactionTemplate, jdbcTemplate, namedParameterJdbcTemplate, contexts, null);
    }

    /**
     * execute update operation, to fix #3617.
     *
     * @param transactionTemplate {@link TransactionTemplate}
     * @param jdbcTemplate        {@link JdbcTemplate}
     * @param contexts            {@link List} ModifyRequest list
     * @return {@link Boolean}
     */
    default Boolean update(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                           List<ModifyRequest> contexts, BiConsumer<Integer, Throwable> consumer) {
        boolean updateResult = Boolean.FALSE;
        try {
            updateResult = transactionTemplate.execute(status -> {

                String[] errSql = new String[]{null};
                Object[][] args = new Object[][]{null};
                try {
                    // 影响的行数
                    AtomicInteger row = new AtomicInteger();

                    contexts.forEach(pair -> {
                        errSql[0] = pair.getSql();
                        args[0] = pair.getArgs();
                        boolean rollBackOnUpdateFail = pair.isRollBackOnUpdateFail();
                        //LOGGER.debug("current sql : {}", errSql[0]);
                        //LOGGER.debug("current args : {}", args[0]);

                        if (pair.getNamedArgs() != null) {
                            row.addAndGet(namedParameterJdbcTemplate.update(pair.getSql(), pair.getNamedArgs()));
                        } else {
                            row.addAndGet(jdbcTemplate.update(pair.getSql(), pair.getArgs()));
                        }
                        //LOGGER.debug("SQL update affected {} rows ", row);
                        if (rollBackOnUpdateFail && row.get() < 1) {
                            //LOGGER.debug("SQL update affected {} rows ", row);
                            throw new IllegalTransactionStateException("Illegal transaction");
                        }
                    });
                    if (consumer != null) {
                        consumer.accept(row.get(), null);
                    }
                    return Boolean.TRUE;
                } catch (BadSqlGrammarException | DataIntegrityViolationException e) {
                    LOGGER.error("[db-error] sql : {}, args : {}, error : {}", errSql[0], args[0], e.toString());
                    if (consumer != null) {
                        consumer.accept(null, e);
                    }
                    return Boolean.FALSE;
                } catch (CannotGetJdbcConnectionException e) {
                    LOGGER.error("[db-error] sql : {}, args : {}, error : {}", errSql[0], args[0], e.toString());
                    throw e;
                } catch (DataAccessException e) {
                    LOGGER.error("[db-error] DataAccessException sql : {}, args : {}, error : {}", errSql[0], args[0],
                            ExceptionHelper.getAllExceptionMsg(e));
                    throw e;
                }
            });
        } catch (IllegalTransactionStateException e) {
            LOGGER.debug("Roll back transaction for {} ", e.getMessage());
            if (consumer != null) {
                consumer.accept(null, e);
            }
        }


        return updateResult;
    }


    default Object runInTransaction(TransactionTemplate transactionTemplate, Function<Object, Object> action, boolean rollBack) throws IllegalTransactionStateException {
        return transactionTemplate.execute(status -> {
            try {
                return action.apply(status);
            } finally {
                if (rollBack) {
                    status.setRollbackOnly();
                }
            }
        });
    }
}
