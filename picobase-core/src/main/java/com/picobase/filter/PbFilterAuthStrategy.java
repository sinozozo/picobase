package com.picobase.filter;

/**
 * 全局过滤器 - 认证策略封装，方便 lambda 表达式风格调用
 */
@FunctionalInterface
public interface PbFilterAuthStrategy {

    /**
     * 执行方法
     *
     * @param obj 无含义参数，留作扩展
     */
    void run(Object obj);

}
