package com.picobase.fun.strategy;


import com.picobase.logic.authz.PbAuthZLogic;

import java.util.function.Function;

/**
 * 函数式接口：创建 StpLogic 的算法
 *
 * <p>  参数：账号体系标识  </p>
 * <p>  返回：创建好的 StpLogic 对象  </p>
 */
@FunctionalInterface
public interface PbCreatePbLogicFunction extends Function<String, PbAuthZLogic> {

}