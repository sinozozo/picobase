package com.picobase.filter;

import com.picobase.error.PbSpringBootErrorCode;
import com.picobase.exception.BackResultException;
import com.picobase.exception.PbException;
import com.picobase.exception.StopMatchException;
import com.picobase.router.PbRouter;
import com.picobase.util.PbConstants;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servlet 全局鉴权过滤器
 * <p>
 * 默认优先级为 -100，尽量保证在其它过滤器之前执行
 * </p>
 */
@Order(PbConstants.ASSEMBLY_ORDER)
public class PbServletFilter implements PbFilter, Filter {

    /**
     * 拦截路由
     */
    public List<String> includeList = new ArrayList<>();
    // ------------------------ 设置此过滤器 拦截 & 放行 的路由
    /**
     * 放行路由
     */
    public List<String> excludeList = new ArrayList<>();
    /**
     * 认证函数：每次请求执行
     */
    public PbFilterAuthStrategy auth = r -> {
    };
    /**
     * 异常处理函数：每次[认证函数]发生异常时执行此函数
     */
    public PbFilterErrorStrategy error = e -> {
        throw new PbException(e).setCode(PbSpringBootErrorCode.CODE_20105);
    };
    /**
     * 前置函数：在每次[认证函数]之前执行
     * <b>注意点：前置认证函数将不受 includeList 与 excludeList 的限制，所有路由的请求都会进入 beforeAuth</b>
     */
    public PbFilterAuthStrategy beforeAuth = r -> {
    };


    // ------------------------ 钩子函数

    @Override
    public PbServletFilter addInclude(String... paths) {
        includeList.addAll(Arrays.asList(paths));
        return this;
    }

    @Override
    public PbServletFilter addExclude(String... paths) {
        excludeList.addAll(Arrays.asList(paths));
        return this;
    }

    @Override
    public PbServletFilter setIncludeList(List<String> pathList) {
        includeList = pathList;
        return this;
    }

    @Override
    public PbServletFilter setExcludeList(List<String> pathList) {
        excludeList = pathList;
        return this;
    }

    @Override
    public PbServletFilter setAuth(PbFilterAuthStrategy auth) {
        this.auth = auth;
        return this;
    }

    @Override
    public PbServletFilter setError(PbFilterErrorStrategy error) {
        this.error = error;
        return this;
    }

    @Override
    public PbServletFilter setBeforeAuth(PbFilterAuthStrategy beforeAuth) {
        this.beforeAuth = beforeAuth;
        return this;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {

            // 执行全局过滤器
            beforeAuth.run(null);
            PbRouter.match(includeList).notMatch(excludeList).check(r -> {
                auth.run(null);
            });

        } catch (StopMatchException e) {
            // StopMatchException 异常代表：停止匹配，进入Controller

        } catch (Throwable e) {
            // 1. 获取异常处理策略结果
            String result = (e instanceof BackResultException) ? e.getMessage() : String.valueOf(error.run(e));

            // 2. 写入输出流
            // 		请注意此处默认 Content-Type 为 text/plain，如果需要返回 JSON 信息，需要在 return 前自行设置 Content-Type 为 application/json
            // 		例如：PbHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
            if (response.getContentType() == null) {
                response.setContentType(PbConstants.CONTENT_TYPE_TEXT_PLAIN);
            }
            response.getWriter().print(result);
            return;
        }

        // 执行
        chain.doFilter(request, response);
    }
}
