package com.picobase.interceptor;

import cn.hutool.core.util.ArrayUtil;

public class Interceptors {

    public static <T, R> R run(T t, InterceptorNextFunc<T, R> next, InterceptorFunc<T, R>... interceptors) {
        int length = ArrayUtil.length(interceptors);
        for (int i = length - 1; i >= 0; i--) {
            next = interceptors[i].run(next);
        }
        return next.run(t);
    }

    public static void main(String[] args) {
        InterceptorFunc a = next -> str -> {
            System.out.println("aaa:" + str);
            return next.run(str);
        };

        InterceptorFunc b = next -> str -> {
            System.out.println("bbb:" + str);
            return next.run(str);
        };

        Interceptors.run("hello", (str) -> {
            System.out.println("zouqiang");
            return "hello";
        }, a, b);
    }

}

