package com.picobase.filter;

import com.picobase.exception.RequestPathInvalidException;
import com.picobase.strategy.PbStrategy;
import com.picobase.util.PbConstants;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Order(PbConstants.PATH_CHECK_FILTER_ORDER)
public class PbPathCheckFilterForServlet implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 校验本次请求 path 是否合法
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            PbStrategy.instance.checkRequestPath.run(req.getRequestURI(), request, response);
        } catch (RequestPathInvalidException e) {
            if (PbStrategy.instance.requestPathInvalidHandle == null) {
                response.setContentType("text/plain; charset=utf-8");
                response.getWriter().print(e.getMessage());
                response.getWriter().flush();
            } else {
                PbStrategy.instance.requestPathInvalidHandle.run(e, request, response);
            }
            return;
        }

        // 向下执行
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }


    @Override
    public void destroy() {
    }
}
