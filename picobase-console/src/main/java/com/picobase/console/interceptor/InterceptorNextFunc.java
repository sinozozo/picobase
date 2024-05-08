package com.picobase.console.interceptor;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface InterceptorNextFunc<T,R>  {

    R run(T t);

}
