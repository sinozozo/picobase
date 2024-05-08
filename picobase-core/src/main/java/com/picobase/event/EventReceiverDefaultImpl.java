package com.picobase.event;

import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Method;

/**
 * 动态代理被EventReceiver注解标注的方法，为了避免反射最终会用javassist字节码增强的方法去代理 EventReceiverDefaultImpl
 *
 */
public class EventReceiverDefaultImpl implements IEventReceiver {

    // 观察者的bean
    private Object bean;

    // 被EventReceiver注解标注的方法
    private Method method;

    // 事件接收方式
    private boolean isAsync;

    // 接收的参数Class
    private Class<? extends PbEvent> eventClazz;

    public EventReceiverDefaultImpl(Object bean, Method method, boolean isAsync, Class<? extends PbEvent> eventClazz) {
        this.bean = bean;
        this.method = method;
        this.isAsync = isAsync;
        this.eventClazz = eventClazz;
        ReflectUtil.setAccessible(this.method);
    }


    @Override
    public boolean isAsync() {
        return this.isAsync;
    }

    @Override
    public void invoke(PbEvent event) {
        ReflectUtil.invoke(bean, method, event);
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }


    public Class<? extends PbEvent> getEventClazz() {
        return eventClazz;
    }




}
