package com.picobase.spring.pathmatch;


import com.picobase.exception.PbException;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 路由匹配工具类
 */
public class PbPatternsRequestConditionHolder {

    private PbPatternsRequestConditionHolder() {
    }

    public static PatternsRequestCondition patternsRequestCondition;

    public static Method matcherMethod;

    static {
        try {
            patternsRequestCondition = new PatternsRequestCondition();
            matcherMethod = PatternsRequestCondition.class.getDeclaredMethod("getMatchingPattern", String.class, String.class);
            matcherMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new PbException("路由匹配器初始化失败", e);
        }
    }

    /**
     * 判断：指定路由匹配符是否可以匹配成功指定路径
     *
     * @param pattern    路由匹配符
     * @param lookupPath 要匹配的路径
     * @return 是否匹配成功
     */
    public static boolean match(String pattern, String lookupPath) {
        try {
            return matcherMethod.invoke(patternsRequestCondition, pattern, lookupPath) != null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PbException("路由匹配器调用失败", e);
        }
    }

	/*
	 	性能测试：
			100万次
			new 对象方式，耗时：3.685s		最慢
			反射调方法方式，耗时：1.311s  	中等
			原始方式，耗时：0.445s  		最快，但有bug
	 */

}
