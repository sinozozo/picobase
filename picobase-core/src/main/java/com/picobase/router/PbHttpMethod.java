package com.picobase.router;


import com.picobase.error.PbErrorCode;
import com.picobase.exception.PbException;

import java.util.HashMap;
import java.util.Map;

/**
 * Http 请求各种请求类型的枚举表示
 *
 * <p> 参考：Spring - HttpMethod
 */
public enum PbHttpMethod {

    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE, CONNECT,

    /**
     * 代表全部请求方式
     */
    ALL;

    private static final Map<String, PbHttpMethod> map = new HashMap<>();

    static {
        for (PbHttpMethod reqMethod : values()) {
            map.put(reqMethod.name(), reqMethod);
        }
    }

    /**
     * String 转 enum
     *
     * @param method 请求类型
     * @return PbHttpMethod 对象
     */
    public static PbHttpMethod toEnum(String method) {
        if (method == null) {
            throw new PbException("Method 不可以是 null").setCode(PbErrorCode.CODE_10321);
        }
        PbHttpMethod reqMethod = map.get(method.toUpperCase());
        if (reqMethod == null) {
            throw new PbException("无效Method：" + method).setCode(PbErrorCode.CODE_10321);
        }
        return reqMethod;
    }

    /**
     * String[] 转 enum[]
     *
     * @param methods 请求类型数组
     * @return PbHttpMethod 数组
     */
    public static PbHttpMethod[] toEnumArray(String... methods) {
        PbHttpMethod[] arr = new PbHttpMethod[methods.length];
        for (int i = 0; i < methods.length; i++) {
            arr[i] = PbHttpMethod.toEnum(methods[i]);
        }
        return arr;
    }

    public boolean matches(String method) {
        return name().equals(method);
    }

}
