package com.picobase.fun;

/**
 * 无形参、无返回值的函数式接口，方便开发者进行 lambda 表达式风格调用
 */
@FunctionalInterface
public interface PbFunction {

    /**
     * 执行的方法
     */
    void run();

}
