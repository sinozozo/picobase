package com.picobase.spring;


import com.picobase.context.PbContext;
import com.picobase.filter.PbPathCheckFilterForServlet;
import org.springframework.context.annotation.Bean;

/**
 * 注册框架所需要的 Bean
 */
public class PbContextRegister {

    /**
     * 获取上下文处理器组件 (Spring版)
     *
     * @return /
     */
    @Bean
    public PbContext getPbContextForSpring() {
        return new PbContextForSpring();
    }

    /**
     * 请求 path 校验过滤器
     *
     * @return /
     */
    @Bean
    public PbPathCheckFilterForServlet pbPathCheckFilterForServlet() {
        return new PbPathCheckFilterForServlet();
    }

}
