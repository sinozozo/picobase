package com.picobase.context.model;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Response 响应对象 包装类
 */
public interface PbResponse {

    /**
     * 指定前端可以获取到哪些响应头时使用的参数名
     */
    String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    /**
     * 获取底层被包装的源对象
     *
     * @return /
     */
    Object getSource();

    /**
     * 删除指定Cookie
     *
     * @param name Cookie名称
     */
    default void deleteCookie(String name) {
        addCookie(name, null, null, null, 0);
    }

    /**
     * 删除指定Cookie
     *
     * @param name   Cookie名称
     * @param path   Cookie 路径
     * @param domain Cookie 作用域
     */
    default void deleteCookie(String name, String path, String domain) {
        addCookie(name, null, path, domain, 0);
    }

    /**
     * 写入指定Cookie
     *
     * @param name    Cookie名称
     * @param value   Cookie值
     * @param path    Cookie路径
     * @param domain  Cookie的作用域
     * @param timeout 过期时间 （秒）
     */
    default void addCookie(String name, String value, String path, String domain, int timeout) {
        this.addCookie(new PbCookie(name, value).setPath(path).setDomain(domain).setMaxAge(timeout));
    }

    /**
     * 写入指定Cookie
     *
     * @param cookie Cookie-Model
     */
    default void addCookie(PbCookie cookie) {
        this.addHeader(PbCookie.HEADER_NAME, cookie.toHeaderValue());
    }

    /**
     * 设置响应状态码
     *
     * @param sc 响应状态码
     * @return 对象自身
     */
    PbResponse setStatus(int sc);

    /**
     * 在响应头里写入一个值
     *
     * @param name  名字
     * @param value 值
     * @return 对象自身
     */
    PbResponse setHeader(String name, String value);

    /**
     * 在响应头里添加一个值
     *
     * @param name  名字
     * @param value 值
     * @return 对象自身
     */
    PbResponse addHeader(String name, String value);

    /**
     * 在响应头写入 [Server] 服务器名称
     *
     * @param value 服务器名称
     * @return 对象自身
     */
    default PbResponse setServer(String value) {
        return this.setHeader("Server", value);
    }

    /**
     * 重定向
     *
     * @param url 重定向地址
     * @return 任意值
     */
    Object redirect(String url);

    void setContentType(String type);

    void setDateHeader(String name, long date);

    OutputStream getOutputStream() throws IOException;

}
