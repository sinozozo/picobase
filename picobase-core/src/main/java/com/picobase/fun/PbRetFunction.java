package com.picobase.fun;

/**
 * 无形参、有返回值的函数式接口，方便开发者进行 lambda 表达式风格调用
 */
@FunctionalInterface
public interface PbRetFunction {

    /**
     * 执行的方法
     *
     * @return 返回值
     */
    Object run();

}
