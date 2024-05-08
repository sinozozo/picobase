package com.picobase.fun.strategy;


import com.picobase.exception.RequestPathInvalidException;

/**
 * 函数式接口：当请求 path 校验不通过时处理方案的算法
 */
@FunctionalInterface
public interface PbRequestPathInvalidHandleFunction {

    /**
     * 执行函数
     *
     * @param e       请求 path 无效的异常对象
     * @param extArg1 扩展参数1
     * @param extArg2 扩展参数2
     */
    void run(RequestPathInvalidException e, Object extArg1, Object extArg2);

}