package com.picobase.interceptor;

@FunctionalInterface
public interface InterceptorNextFunc<T, R> {

    R run(T t);

}
