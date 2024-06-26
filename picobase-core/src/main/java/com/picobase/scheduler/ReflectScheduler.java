package com.picobase.scheduler;


import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Method;

/**
 * 动态代理被Scheduler注解标注的方法，为了避免反射最终会用javassist字节码增强的方法去代理ReflectScheduler
 */
public class ReflectScheduler implements IScheduler {

    private Object bean;

    private Method method;

    public static ReflectScheduler valueOf(Object bean, Method method) {
        var scheduler = new ReflectScheduler();
        scheduler.bean = bean;
        scheduler.method = method;
        return scheduler;
    }

    @Override
    public void invoke() {
        ReflectUtil.invoke(bean, method);
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }
}
