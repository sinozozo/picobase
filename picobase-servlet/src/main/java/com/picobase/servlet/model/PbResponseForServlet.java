package com.picobase.servlet.model;

import com.picobase.context.model.PbResponse;
import com.picobase.exception.PbException;
import com.picobase.servlet.error.PbServletErrorCode;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 对 PbResponse 包装类的实现（Servlet 版）
 */
public class PbResponseForServlet implements PbResponse {

    /**
     * 底层Request对象
     */
    protected HttpServletResponse response;

    /**
     * 实例化
     *
     * @param response response对象
     */
    public PbResponseForServlet(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * 获取底层源对象
     */
    @Override
    public Object getSource() {
        return response;
    }

    /**
     * 设置响应状态码
     */
    @Override
    public PbResponse setStatus(int sc) {
        response.setStatus(sc);
        return this;
    }

    /**
     * 在响应头里写入一个值
     */
    @Override
    public PbResponse setHeader(String name, String value) {
        response.setHeader(name, value);
        return this;
    }

    /**
     * 在响应头里添加一个值
     *
     * @param name  名字
     * @param value 值
     * @return 对象自身
     */
    public PbResponse addHeader(String name, String value) {
        response.addHeader(name, value);
        return this;
    }

    public void setContentType(String type) {
        response.setContentType(type);
    }

    public void setDateHeader(String name, long date) {
        response.setDateHeader(name, date);
    }


    /**
     * 重定向
     */
    @Override
    public Object redirect(String url) {
        try {
            response.sendRedirect(url);
        } catch (Exception e) {
            throw new PbException(e).setCode(PbServletErrorCode.CODE_20002);
        }
        return null;
    }


    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }
}
