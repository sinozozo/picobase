package com.picobase.servlet.model;

import com.picobase.PbManager;
import com.picobase.application.ApplicationInfo;
import com.picobase.context.model.PbRequest;
import com.picobase.exception.PbException;
import com.picobase.file.PbFile;
import com.picobase.servlet.error.PbServletErrorCode;
import com.picobase.util.CommonHelper;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对 PbRequest 包装类的实现（Servlet 版）
 */
public class PbRequestForServlet extends PbRequestWithContentCache implements PbRequest {

    /**
     * 底层Request对象
     */
    protected HttpServletRequest request;

    /**
     * 实例化
     *
     * @param request request对象
     */
    public PbRequestForServlet(HttpServletRequest request) {
        super(request);
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
     * 在 [请求体] 里获取一个值
     */
    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    /**
     * 获取 [请求体] 里提交的所有参数名称
     *
     * @return 参数名称列表
     */
    @Override
    public List<String> getParamNames() {
        Enumeration<String> parameterNames = request.getParameterNames();
        List<String> list = new ArrayList<>();
        while (parameterNames.hasMoreElements()) {
            list.add(parameterNames.nextElement());
        }
        return list;
    }

    /**
     * 获取 [请求体] 里提交的所有参数
     *
     * @return 参数列表
     */
    @Override
    public Map<String, String[]> getParamMap() {
        // 获取所有参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String[]> map = new LinkedHashMap<>(parameterMap.size());
        for (String key : parameterMap.keySet()) {
            String[] values = parameterMap.get(key);
            map.put(key, values);
        }
        return map;
    }

    /**
     * 在 [请求头] 里获取一个值
     */
    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    /**
     * 在 [Cookie作用域] 里获取一个值
     */
    @Override
    public String getCookieValue(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie != null && name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 返回当前请求path (不包括上下文名称)
     */
    @Override
    public String getRequestPath() {
        return ApplicationInfo.cutPathPrefix(request.getRequestURI());
    }

    /**
     * 返回当前请求的url，例：http://xxx.com/test
     *
     * @return see note
     */
    public String getUrl() {
        String currDomain = PbManager.getConfig().getCurrDomain();
        if (!CommonHelper.isEmpty(currDomain)) {
            return currDomain + this.getRequestPath();
        }
        return request.getRequestURL().toString();
    }

    /**
     * 返回当前请求的类型
     */
    @Override
    public String getMethod() {
        return request.getMethod();
    }

    /**
     * 转发请求
     */
    @Override
    public Object forward(String path) {
        try {
            HttpServletResponse response = (HttpServletResponse) PbManager.getPbContext().getResponse().getSource();
            request.getRequestDispatcher(path).forward(request, response);
            return null;
        } catch (ServletException | IOException e) {
            throw new PbException(e).setCode(PbServletErrorCode.CODE_20001);
        }
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public List<PbFile> getPart(String name) {
        try {
            return request.getParts().stream().filter(part -> part.getName().equals(name) && part.getSize() > 0).map(part ->
                    {
                        try {
                            return PbFile.newFileFromMultipart(part.getName(),
                                    part.getSubmittedFileName(),
                                    part.getSize(),
                                    part.getContentType(),
                                    part.getInputStream());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).collect(Collectors.toList());
        } catch (IOException | ServletException e) {
            throw new PbException(e);
        }
    }


}
