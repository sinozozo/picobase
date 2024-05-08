package com.picobase.console.mapper;

import com.picobase.PbManager;
import com.picobase.log.PbLog;
import com.picobase.model.AdminModel;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.mapper.MapperProxy;
import com.picobase.persistence.mapper.PbMapper;
import com.picobase.persistence.model.MapperResult;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyMapper implements MethodInterceptor {
    private static final PbLog LOGGER = PbManager.getLog();

    private static final Map<String, PbMapper> SINGLE_MAPPER_PROXY_MAP = new ConcurrentHashMap<>(16);

    /**
     * create proxy-mapper single instead of using method createProxy.
     */
    public static <R> R createSingleProxy(PbMapper mapper) {
        return (R) SINGLE_MAPPER_PROXY_MAP.computeIfAbsent(mapper.getClass().getSimpleName(), key ->
                new ProxyMapper().createProxy(mapper));
    }

    public <R> R createProxy(PbMapper mapper) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(mapper.getClass());
        enhancer.setCallback(this);
        return (R) enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object invoke = methodProxy.invokeSuper(o,objects);

        String className = o.getClass().getSimpleName();
        String methodName = method.getName();
        String sql;
        if (invoke instanceof MapperResult mr) {
            sql = mr.getSql();
        } else if (invoke instanceof SelectQuery sq) {
            sql = sq.build().getSql();
        } else {
            sql = invoke.toString();
        }
        LOGGER.info("[{}] METHOD : {}, SQL : {}, ARGS : {}", className, methodName, sql, PbManager.getPbJsonTemplate().toJsonString(objects));
        return invoke;
    }
}
