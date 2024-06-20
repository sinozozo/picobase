package com.picobase.event;

import cn.hutool.core.util.ArrayUtil;
import com.picobase.PbManager;
import com.picobase.annotation.PbEventReceiver;
import com.picobase.log.PbLog;
import com.picobase.util.StrFormatter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class PbEventRegisterProcessor {

    public PbLog log = PbManager.getLog();
    /**
     * 是否启用字节码增强
     */
    public boolean enhance;
    private PbEventBus eventBus;


    public PbEventRegisterProcessor(PbEventBus eventBus, boolean enhance) {
        this.eventBus = eventBus;
        this.enhance = enhance;
    }

    /**
     * 从一个Class中获得具有指定注解的Method，只获取子类的Method，不获取父类的Method
     *
     * @param clazz      指定的Class
     * @param annotation 指定注解的Class
     * @return 数组，可能长度为0
     */
    private static Method[] getMethodsByAnnoInPOJOClass(Class<?> clazz, Class<? extends Annotation> annotation) {
        var list = new ArrayList<Method>();
        var methods = clazz.getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(annotation)) {
                list.add(method);
            }
        }
        return ArrayUtil.toArray(list, Method.class);

    }

    private static boolean isPojoClass(Class<?> clazz) {
        return clazz.getSuperclass().equals(Object.class) || clazz.isRecord();
    }

    public Object postProcessAfterInitialization(Object bean) {
        var clazz = bean.getClass();
        var methods = getMethodsByAnnoInPOJOClass(clazz, PbEventReceiver.class);
        if (ArrayUtil.isEmpty(methods)) {
            return bean;
        }

        if (!isPojoClass(clazz)) {
            log.warn("The message registration class [{}] is not a POJO class, and the parent class will not be scanned", clazz);
        }

        try {
            for (var method : methods) {
                var paramClazzs = method.getParameterTypes();
                if (paramClazzs.length != 1) {
                    throw new IllegalArgumentException(StrFormatter.format("[class:{}] [method:{}] must have one parameter!", bean.getClass().getName(), method.getName()));
                }
                if (!PbEvent.class.isAssignableFrom(paramClazzs[0])) {
                    throw new IllegalArgumentException(StrFormatter.format("[class:{}] [method:{}] must have one [IEvent] type parameter!", bean.getClass().getName(), method.getName()));
                }
                @SuppressWarnings("unchecked")
                var eventClazz = (Class<? extends PbEvent>) paramClazzs[0];
                var eventName = eventClazz.getCanonicalName();
                var methodName = method.getName();

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException(StrFormatter.format("[class:{}] [method:{}] [eventhandler:{}] must use 'public' as modifier!", bean.getClass().getName(), methodName, eventName));
                }

                if (Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException(StrFormatter.format("[class:{}] [method:{}] [eventhandler:{}] can not use 'static' as modifier!", bean.getClass().getName(), methodName, eventName));
                }

                var expectedMethodName = StrFormatter.format("on{}", eventClazz.getSimpleName());
                if (!methodName.equals(expectedMethodName)) {
                    throw new IllegalArgumentException(StrFormatter.format("[class:{}] [method:{}] [eventhandler:{}] expects '{}' as method name!"
                            , bean.getClass().getName(), methodName, eventName, expectedMethodName));
                }

                var isAsync = method.getDeclaredAnnotation(PbEventReceiver.class).isAsync();
                var receiverDefinition = new EventReceiverDefaultImpl(bean, method, isAsync, eventClazz);
                // key:class类型 value:观察者 注册Event的receiverMap中
                eventBus.registerEventReceiver(eventClazz, enhance ? EnhanceUtils.createEventReceiver(receiverDefinition) : receiverDefinition);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return bean;
    }

}
