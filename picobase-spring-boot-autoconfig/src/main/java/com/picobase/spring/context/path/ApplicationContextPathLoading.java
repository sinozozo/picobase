package com.picobase.spring.context.path;

import com.picobase.application.ApplicationInfo;
import com.picobase.util.CommonHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 应用上下文路径加载器
 */
public class ApplicationContextPathLoading implements ApplicationRunner {

    @Value("${server.servlet.context-path:}")
    String contextPath;

    @Value("${spring.mvc.servlet.path:}")
    String servletPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String routePrefix = "";

        if (CommonHelper.isNotEmpty(contextPath)) {
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
            routePrefix += contextPath;
        }

        if (CommonHelper.isNotEmpty(servletPath)) {
            if (!servletPath.startsWith("/")) {
                servletPath = "/" + servletPath;
            }
            if (servletPath.endsWith("/")) {
                servletPath = servletPath.substring(0, servletPath.length() - 1);
            }
            routePrefix += servletPath;
        }

        if (CommonHelper.isNotEmpty(routePrefix) && !routePrefix.equals("/")) {
            ApplicationInfo.routePrefix = routePrefix;
        }
    }

}