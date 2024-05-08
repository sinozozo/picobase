package com.picobase.spring.pathmatch;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 路由匹配工具类：使用 PathPatternParser 模式匹配
 */
public class PbPathPatternParserUtil {

    private PbPathPatternParserUtil() {
    }

    /**
     * 判断：指定路由匹配符是否可以匹配成功指定路径
     *
     * @param pattern 路由匹配符
     * @param path    要匹配的路径
     * @return 是否匹配成功
     */
    public static boolean match(String pattern, String path) {
        PathPattern pathPattern = PathPatternParser.defaultInstance.parse(pattern);
        PathContainer pathContainer = PathContainer.parsePath(path);
        return pathPattern.matches(pathContainer);
    }

	/*
		表现：
			springboot 2.x SpringMVC	match("/test/test", "/test/test/")  // true
			springboot 2.x WebFlux		match("/test/test", "/test/test/")  // true
			springboot 3.x SpringMVC	match("/test/test", "/test/test/")  // false
			springboot 3.x WebFlux		match("/test/test", "/test/test/")  // false
	 */

}
