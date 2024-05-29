package com.picobase.persistence.mapper;


import com.picobase.PbManager;
import com.picobase.log.PbLog;
import com.picobase.persistence.model.MapperResult;
import com.picobase.persistence.repository.PbRowMapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataSource plugin PbMapper sql proxy.
 **/
@Deprecated
public class MapperProxy implements InvocationHandler {

    private static final PbLog LOGGER = PbManager.getLog();

    private PbMapper mapper;

    private static final Map<String, PbMapper> SINGLE_MAPPER_PROXY_MAP = new ConcurrentHashMap<>(16);

    public <R> R createProxy(PbMapper mapper) {
        this.mapper = mapper;
        return (R) Proxy.newProxyInstance(MapperProxy.class.getClassLoader(), mapper.getClass().getInterfaces(), this);
    }

    /**
     * create proxy-mapper single instead of using method createProxy.
     */
    public static <R> R createSingleProxy(PbMapper mapper) {
        return (R) SINGLE_MAPPER_PROXY_MAP.computeIfAbsent(mapper.getClass().getSimpleName(), key ->
                new MapperProxy().createProxy(mapper));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object invoke = method.invoke(mapper, args);

        String className = mapper.getClass().getSimpleName();
        String methodName = method.getName();
        String sql;
        if (invoke instanceof MapperResult) {
            sql = ((MapperResult) invoke).getSql();
        } else {
            sql = invoke.toString();
        }
        LOGGER.info("[{}] METHOD : {}, SQL : {}, ARGS : {}", className, methodName, sql, PbManager.getPbJsonTemplate().toJsonString(args));
        return invoke;
    }


}
