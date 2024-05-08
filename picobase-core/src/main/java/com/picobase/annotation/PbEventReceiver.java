package com.picobase.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PbEventReceiver {
    /**
     * 是否异步执行,默认在当前线程下执行
     * @return
     */
    boolean isAsync() default false;
}
