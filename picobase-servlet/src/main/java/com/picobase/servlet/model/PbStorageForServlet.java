package com.picobase.servlet.model;


import com.picobase.context.model.PbStorage;

import javax.servlet.http.HttpServletRequest;

/**
 * 对 PbStorage 包装类的实现（Servlet 版）
 */
public class PbStorageForServlet implements PbStorage {

    /**
     * 底层Request对象
     */
    protected HttpServletRequest request;

    /**
     * 实例化
     *
     * @param request request对象
     */
    public PbStorageForServlet(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 获取底层源对象
     */
    @Override
    public Object getSource() {
        return request;
    }

    /**
     * 在 [Request作用域] 里写入一个值
     */
    @Override
    public PbStorageForServlet set(String key, Object value) {
        request.setAttribute(key, value);
        return this;
    }

    /**
     * 在 [Request作用域] 里获取一个值
     */
    @Override
    public Object get(String key) {
        return request.getAttribute(key);
    }

    /**
     * 在 [Request作用域] 里删除一个值
     */
    @Override
    public PbStorageForServlet delete(String key) {
        request.removeAttribute(key);
        return this;
    }

}
