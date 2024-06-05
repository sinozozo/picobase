package com.picobase.persistence.repository;

import cn.hutool.core.map.SafeConcurrentHashMap;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.ServiceLoaderUtil;
import cn.hutool.core.util.TypeUtil;
import com.picobase.PbManager;
import com.picobase.exception.PbException;

import java.lang.reflect.Type;
import java.util.Map;

public class PbRowMapperRegistry {

    /**
     * 默认类型PbRowMapper
     */
    private final Map<Class<?>, PbRowMapper<?>> defaultRowMapperMap;

    /**
     * 用户自定义PbRowMapper
     */
    private volatile Map<Type, PbRowMapper<?>> customRowMapperMap;

    private PbRowMapperRegistry() {
        this.defaultRowMapperMap = new SafeConcurrentHashMap<>();
        putCustomBySpi();
    }

    /**
     * 获得单例的 PbRowMapperRegistry
     *
     * @return PbRowMapperRegistry
     */
    public static PbRowMapperRegistry getInstance() {
        return PbRowMapperRegistry.SingletonHolder.INSTANCE;
    }

    /**
     * 使用SPI加载PbRowMapper
     */
    private void putCustomBySpi() {
        ServiceLoaderUtil.load(PbRowMapper.class).forEach(rowMapper -> {
            try {
                Type type = TypeUtil.getTypeArgument(ClassUtil.getClass(rowMapper));
                if (null != type) {
                    putCustom(type, rowMapper);
                }
            } catch (Exception e) {
                throw new PbException("SPI Load PbRowMapper error!", e);
            }
        });
    }


    /**
     * 登记自定义PbRowMapper
     *
     * @param type           映射的目标类型
     * @param rowMapperClass PbRowMapper类，必须有默认构造方法
     * @return PbRowMapperRegistry
     */
    public PbRowMapperRegistry putCustom(Type type, Class<? extends PbRowMapper<?>> rowMapperClass) {
        return putCustom(type, ReflectUtil.newInstance(rowMapperClass));
    }

    /**
     * 登记自定义PbRowMapper
     *
     * @param type           映射的目标类型
     * @param rowMapperClass PbRowMapper
     * @return ConverterRegistry
     */
    public PbRowMapperRegistry putCustom(Type type, PbRowMapper<?> rowMapperClass) {
        if (null == customRowMapperMap) {
            synchronized (this) {
                if (null == customRowMapperMap) {
                    customRowMapperMap = new SafeConcurrentHashMap<>();
                }
            }
        }
        customRowMapperMap.put(type, rowMapperClass);
        return this;
    }

    /**
     * 获得PbRowMapper<br>
     *
     * @param <T>           映射的目标类型
     * @param type          类型
     * @param isCustomFirst 是否自定义PbRowMapper优先
     * @return PbRowMapper
     */
    public <T> PbRowMapper<T> getPbRowMapper(Type type, boolean isCustomFirst) {
        PbRowMapper<T> rowMapper;
        if (isCustomFirst) {
            rowMapper = this.getCustomRowMapper(type);
            if (null == rowMapper) {
                rowMapper = this.getDefaultRowMapper(type);
            }
        } else {
            rowMapper = this.getDefaultRowMapper(type);
            if (null == rowMapper) {
                rowMapper = this.getCustomRowMapper(type);
            }
        }
        return rowMapper;
    }


    /**
     * 获得默认PbRowMapper
     *
     * @param <T>  映射的目标类型（PbRowMapper转换到的类型）
     * @param type 类型
     * @return PbRowMapper
     */
    @SuppressWarnings("unchecked")
    public <T> PbRowMapper<T> getDefaultRowMapper(Type type) {
        final Class<?> key = TypeUtil.getClass(type);
        return (PbRowMapper<T>) defaultRowMapperMap.computeIfAbsent(key, k -> PbManager.getPbRowMapperFactory().getPbRowMapper(key));
    }


    /**
     * 获得自定义PbRowMapper
     *
     * @param <T>  映射的目标类型（PbRowMapper转换到的类型）
     * @param type 类型
     * @return PbRowMapper
     */
    @SuppressWarnings("unchecked")
    public <T> PbRowMapper<T> getCustomRowMapper(Type type) {
        return (null == customRowMapperMap) ? null : (PbRowMapper<T>) customRowMapperMap.get(type);
    }


    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final PbRowMapperRegistry INSTANCE = new PbRowMapperRegistry();
    }
}
