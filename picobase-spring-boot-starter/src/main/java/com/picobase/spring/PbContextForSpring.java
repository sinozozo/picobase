package com.picobase.spring;

import com.picobase.context.PbContext;
import com.picobase.context.model.PbRequest;
import com.picobase.context.model.PbResponse;
import com.picobase.context.model.PbStorage;
import com.picobase.servlet.model.PbRequestForServlet;
import com.picobase.servlet.model.PbResponseForServlet;
import com.picobase.servlet.model.PbStorageForServlet;
import com.picobase.spring.pathmatch.PbPatternsRequestConditionHolder;

/**
 * 上下文处理器 [ SpringMVC版本实现 ]。在 SpringMVC、SpringBoot 中使用 Picobase 时，必须注入此实现类，否则会出现上下文无效异常
 */
public class PbContextForSpring implements PbContext {

    /**
     * 获取当前请求的 Request 包装对象
     */
    @Override
    public PbRequest getRequest() {
        return new PbRequestForServlet(SpringMVCUtil.getRequest());
    }

    /**
     * 获取当前请求的 Response 包装对象
     */
    @Override
    public PbResponse getResponse() {
        return new PbResponseForServlet(SpringMVCUtil.getResponse());
    }

    /**
     * 获取当前请求的 Storage 包装对象
     */
    @Override
    public PbStorage getStorage() {
        return new PbStorageForServlet(SpringMVCUtil.getRequest());
    }

    /**
     * 判断：指定路由匹配符是否可以匹配成功指定路径
     */
    @Override
    public boolean matchPath(String pattern, String path) {
        return PbPatternsRequestConditionHolder.match(pattern, path);
    }

    /**
     * 判断：在本次请求中，此上下文是否可用。
     */
    @Override
    public boolean isValid() {
        return SpringMVCUtil.isWeb();
    }

}
