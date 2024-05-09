package com.picobase.context;


import com.picobase.context.model.PbRequest;
import com.picobase.context.model.PbResponse;
import com.picobase.context.model.PbStorage;

import java.util.Optional;

/**
 * 上下文处理器
 *
 * <p> 上下文处理器封装了当前应用环境的底层操作 </p>
 */
public interface PbContext {

    /**
     * 获取当前请求的 Request 包装对象
     *
     * @return /
     * @see PbRequest
     */
    PbRequest getRequest();

    /**
     * 获取当前请求的 Response 包装对象
     *
     * @return /
     * @see PbResponse
     */
    PbResponse getResponse();

    /**
     * 获取当前请求的 Storage 包装对象
     *
     * @return /
     * @see PbStorage
     */
    PbStorage getStorage();

    /**
     * 判断：指定路由匹配符是否可以匹配成功指定路径
     * <pre>
     *     判断规则由底层 web 框架决定，例如在 springboot 中：
     *     	- matchPath("/user/*", "/user/login")  返回: true
     *     	- matchPath("/user/*", "/article/edit")  返回: false
     * </pre>
     *
     * @param pattern 路由匹配符
     * @param path    需要匹配的路径
     * @return /
     */
    boolean matchPath(String pattern, String path);

    /**
     * 判断：在本次请求中，此上下文是否可用。
     * <p> 例如在部分 rpc 调用时， 一级上下文会返回 false，这时候框架就会选择使用二级上下文来处理请求 </p>
     *
     * @return /
     */
    default boolean isValid() {
        return false;
    }



    /**
     *  将 request 数据(path params, query params and the request body)绑定到对象上 （支持提交的数据格式为 form 或 json）
     *  @param dto 待进行数据绑定的对象
     */
    <T> Optional<T> createObjFromRequest(Class<T> dto);


    void bindRequestTo(Object obj);
}
