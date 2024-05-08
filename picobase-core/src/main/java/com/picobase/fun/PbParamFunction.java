package com.picobase.fun;

/**
 * 单形参、无返回值的函数式接口，方便开发者进行 lambda 表达式风格调用
 */
@FunctionalInterface
public interface PbParamFunction<T> {

    /**
     * 执行的方法
     *
     * @param r 传入的参数
     */
    void run(T r);

}
