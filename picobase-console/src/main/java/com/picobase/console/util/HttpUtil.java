package com.picobase.console.util;

import cn.hutool.core.util.URLUtil;
import org.apache.catalina.connector.RequestFacade;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class HttpUtil {
    /**
     * 获得完整 request 请求Url
     *
     * @param request
     * @return
     */
    public static String getFullRequestUrl(ServletRequest request) {
        String requestUri = ((RequestFacade) request).getRequestURI();
        String queryString = URLUtil.decode(((RequestFacade) request).getQueryString()); //解码query 内容

        if (queryString != null) {
            return requestUri + "?" + queryString;
        } else {
            return requestUri;
        }
    }

    public static String realUserIp(HttpServletRequest request) {
        String ip = request.getHeader("CF-Connecting-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }

        ip = request.getHeader("Fly-Client-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }

        String ipsList = request.getHeader("X-Forwarded-For");
        if (ipsList != null && !ipsList.isEmpty()) {
            String[] ips = ipsList.split(",");
            for (String currentIp : ips) {
                ip = currentIp.trim();
                if (ip != null && !ip.isEmpty()) {
                    return ip;
                }
            }
        }

        return request.getRemoteAddr();
    }
}
