package com.picobase.interceptor;

public interface InterceptorFunc<T, R> {

    InterceptorNextFunc<T, R> run(InterceptorNextFunc<T, R> next);
}
