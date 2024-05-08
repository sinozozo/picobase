package com.picobase.fun.strategy;

/**
 * 函数式接口：校验请求 path 的算法
 *
 * <p>  如果属于无效请求 path，则抛出异常 RequestPathInvalidException  </p>
 */
@FunctionalInterface
public interface PbCheckRequestPathFunction {

    /**
     * 执行函数
     *
     * @param path    请求 path
     * @param extArg1 扩展参数1
     * @param extArg2 扩展参数2
     */
    void run(String path, Object extArg1, Object extArg2);

}