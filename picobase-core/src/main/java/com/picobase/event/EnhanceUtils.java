package com.picobase.event;

import com.picobase.util.StrFormatter;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.hutool.core.text.CharSequenceUtil.upperFirst;

public abstract class EnhanceUtils {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    static {
        // 适配Tomcat，因为Tomcat不是用的默认的类加载器，而Javassist用的是默认的加载器
        var classArray = new Class<?>[]{
                IEventReceiver.class,
                PbEvent.class
        };

        var classPool = ClassPool.getDefault();

        for (var clazz : classArray) {
            if (classPool.find(clazz.getName()) == null) {
                ClassClassPath classPath = new ClassClassPath(clazz);
                classPool.insertClassPath(classPath);
            }
        }
    }


    public static IEventReceiver createEventReceiver(EventReceiverDefaultImpl definition) throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var classPool = ClassPool.getDefault();

        Object bean = definition.getBean();
        Method method = definition.getMethod();
        Class<?> clazz = definition.getEventClazz();
        // 定义类名称
        CtClass enhanceClazz = classPool.makeClass(EnhanceUtils.class.getName() + "$" + upperFirst(bean.getClass().getSimpleName()) + ATOMIC_INTEGER.incrementAndGet());
        enhanceClazz.addInterface(classPool.get(IEventReceiver.class.getName()));

        // 定义类中的一个成员bean
        CtClass beanClass = classPool.get(bean.getClass().getName());
        CtField field = new CtField(beanClass, "bean", enhanceClazz);
        field.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
        enhanceClazz.addField(field);

        // 定义类的构造器
        // 创建构造函数参数数组
        CtClass[] parameterTypes = {beanClass};
        CtConstructor constructor = new CtConstructor(parameterTypes, enhanceClazz);
        constructor.setBody("{this.bean=$1;}");
        constructor.setModifiers(Modifier.PUBLIC);
        enhanceClazz.addConstructor(constructor);

        // 定义类实现的接口方法invoker
        CtMethod invokeMethod = new CtMethod(classPool.get(void.class.getName()), "invoke", classPool.get(new String[]{PbEvent.class.getName()}), enhanceClazz);
        invokeMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String invokeMethodBody = StrFormatter.format("{ this.bean.{}(({})$1); }", method.getName(), clazz.getName()); // 强制类型转换，转换为具体的Event类型的类型
        invokeMethod.setBody(invokeMethodBody);
        enhanceClazz.addMethod(invokeMethod);

        // 定义类实现的接口方法bus
        CtMethod busMethod = new CtMethod(classPool.get(boolean.class.getName()), "isAsync", null, enhanceClazz);
        busMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String busMethodBody = StrFormatter.format("{ return {}; }", definition.isAsync());
        busMethod.setBody(busMethodBody);
        enhanceClazz.addMethod(busMethod);

        // 定义类实现的接口方法getBean
        CtMethod beanMethod = new CtMethod(classPool.get(Object.class.getName()), "getBean", null, enhanceClazz);
        beanMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String beanMethodBody = "{ return this.bean; }";
        beanMethod.setBody(beanMethodBody);
        enhanceClazz.addMethod(beanMethod);

        // 释放缓存
        enhanceClazz.detach();

        Class<?> resultClazz = enhanceClazz.toClass(IEventReceiver.class);
        Constructor<?> resultConstructor = resultClazz.getConstructor(bean.getClass());
        return (IEventReceiver) resultConstructor.newInstance(bean);
    }
}
