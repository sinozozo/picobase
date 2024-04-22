package com.picobase.dao;

/**
 * PicoBase 数据库操作 Dao
 */
public interface PbDao {

    /**
     * 当此 PbDao 实例被装载时触发
     */
    default void init() {
    }

    /**
     * 当此 PbDao 实例被卸载时触发
     */
    default void destroy() {
    }


}
