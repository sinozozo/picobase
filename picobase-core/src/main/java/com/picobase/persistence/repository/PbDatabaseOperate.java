package com.picobase.persistence.repository;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 数据库操作顶层接口
 */
public interface PbDatabaseOperate {

    /**
     * Data query transaction.
     *
     * @param sql sql text
     * @param cls target type
     * @param <R> return type
     * @return query result
     */
    <R> R queryOne(String sql, Class<R> cls);

    /**
     * Data query transaction.
     *
     * @param sql  sqk text
     * @param args sql parameters
     * @param cls  target type
     * @param <R>  return type
     * @return query result
     */
    <R> R queryOne(String sql, Object[] args, Class<R> cls);


    <R> R queryOne(String sql, Map<String, Object> args, Class<R> cls);


    /**
     * Data query transaction.
     *
     * @param sql    sqk text
     * @param args   sql parameters
     * @param mapper Database query result converter
     * @param <R>    return type
     * @return query result
     */
    <R> R queryOne(String sql, Object[] args, PbRowMapper<R> mapper);

    <R> R queryOne(String sql, Map<String, Object> args, PbRowMapper<R> mapper);

    /**
     * Data query transaction.
     *
     * @param sql    sqk text
     * @param args   sql parameters
     * @param mapper Database query result converter
     * @param <R>    return type
     * @return query result
     */
    <R> List<R> queryMany(String sql, Object[] args, PbRowMapper<R> mapper);

    <R> List<R> queryMany(String sql, Map<String, Object> args, PbRowMapper<R> mapper);

    /**
     * Data query transaction.
     *
     * @param sql    sqk text
     * @param args   sql parameters
     * @param rClass target type
     * @param <R>    return type
     * @return query result
     */
    <R> List<R> queryMany(String sql, Object[] args, Class<R> rClass);

    <R> List<R> queryMany(String sql, Map<String, Object> args, Class<R> rClass);

    /**
     * Data query transaction.
     *
     * @param sql  sqk text
     * @param args sql parameters
     * @return query result
     */
    List<Map<String, Object>> queryMany(String sql, Object[] args);

    List<Map<String, Object>> queryMany(String sql, Map<String, Object> args);

    /**
     * data modify transaction.
     *
     * @param modifyRequests {@link List}
     * @param consumer       {@link BiConsumer}
     * @return is success
     */
    Boolean update(List<ModifyRequest> modifyRequests, BiConsumer<Integer, Throwable> consumer);

    /**
     * 在事务中执行action
     *
     * @param action   第一个object
     * @param rollBack 事务最终是否回滚
     * @return
     */
    Object runInTransaction(Function<Object, Object> action, boolean rollBack);

    /**
     * data modify transaction.
     *
     * @param modifyRequests {@link List}
     * @return is success
     */
    default Boolean update(List<ModifyRequest> modifyRequests) {
        return update(modifyRequests, null);
    }


    /**
     * data modify transaction The SqlContext to be executed in the current thread will be executed and automatically
     * cleared.
     *
     * @return is success
     */
    default Boolean blockUpdate() {
        return blockUpdate(null);
    }

    /**
     * data modify transaction The SqlContext to be executed in the current thread will be executed and automatically
     * cleared.
     *
     * @param consumer the consumer
     * @return java.lang.Boolean
     */
    default Boolean blockUpdate(BiConsumer<Integer, Throwable> consumer) {
        try {
            return update(StorageContextHolder.getCurrentSqlContext(), consumer);
        } finally {
            StorageContextHolder.cleanAllContext();
        }
    }

    /**
     * data modify transaction The SqlContext to be executed in the current thread will be executed and automatically
     * cleared.
     *
     * @return is success
     */
    default CompletableFuture<Integer> futureUpdate() {
        try {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            update(StorageContextHolder.getCurrentSqlContext(), (o, throwable) -> {
                if (Objects.nonNull(throwable)) {
                    future.completeExceptionally(throwable);
                    return;
                }
                future.complete(o);
            });
            return future;
        } finally {
            StorageContextHolder.cleanAllContext();
        }
    }

}
