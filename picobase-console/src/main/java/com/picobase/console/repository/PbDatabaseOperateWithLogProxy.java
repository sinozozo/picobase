package com.picobase.console.repository;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.picobase.PbManager;
import com.picobase.log.PbLog;
import com.picobase.persistence.repository.PbDatabaseOperate;
import com.picobase.spring.repository.MysqlDatabaseOperateImpl;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;
import java.util.List;


/**
 * 用于代理 {@link MysqlDatabaseOperateImpl} ，执行sql语句的打印
 */
public class PbDatabaseOperateWithLogProxy implements MethodInterceptor {
    private static final PbLog LOGGER = PbManager.getLog();

    public PbDatabaseOperate create(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MysqlDatabaseOperateImpl.class);
        enhancer.setCallback(this);
        return (PbDatabaseOperate) enhancer.create(new Class[]{JdbcTemplate.class, NamedParameterJdbcTemplate.class, TransactionTemplate.class}, new Object[]{jdbcTemplate, namedParameterJdbcTemplate, transactionTemplate});
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        Object invoke = methodProxy.invokeSuper(o, objects);

        if (method.getDeclaringClass() != MysqlDatabaseOperateImpl.class) {//过滤掉实现类以外的日志,否则会在日志中打印很多无用的信息
            return invoke;
        }

        String methodName = method.getName();
        LOGGER.debug("[{}ms] {}: {}", stopWatch.cost(), StrUtil.truncateUtf8(JSONUtil.toJsonStr(objects), 256), prettyPrintResult(invoke));

        return invoke;
    }

    public String prettyPrintResult(Object result) {
        //如果是集合打印集合的个数，和集合中包含的第一个元素 className
        if (result instanceof List col) {
            if (col.isEmpty()) {
                return "List<> size:0";
            }
            String simpleName = col.iterator().next().getClass().getSimpleName();
            return StrUtil.format("List<{}> size:{}", simpleName, col.size());
        } else {
            return String.valueOf(result);
        }
    }

}

class StopWatch {
    private final long startTime = System.currentTimeMillis();

    public long cost() {
        return System.currentTimeMillis() - startTime;
    }
}