package com.picobase.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.console.config.PbConsoleConfig;
import com.picobase.console.json.SchemaDeserializer;
import com.picobase.console.json.mixin.AdminModelMixIn;
import com.picobase.context.PbHolder;
import com.picobase.exception.PbException;
import com.picobase.filter.PbServletFilter;
import com.picobase.json.PbJsonTemplate;
import com.picobase.jwt.PbAuthZLogicJwtForStateless;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.model.AdminModel;
import com.picobase.model.schema.Schema;
import com.picobase.router.PbRouter;
import com.picobase.spring.json.PbJsonTemplateForJackson;
import com.picobase.spring.json.PbJsonTemplateForJacksonTurbo;
import com.picobase.util.PbConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Admin 相关 Bean 注册
 */
@Configuration
public class PbConsoleRegister {
    /**
     * 使用一个比较短的前缀，尽量提高 cmd 命令台启动时指定参数的便利性
     */
    public static final String CONFIG_PREFIX = "pb-console";

    /**
     * 注册 PbConsoleConfig 配置对象
     *
     * @return PbConsoleConfig 对象
     */
    @Bean
    @ConfigurationProperties(prefix = CONFIG_PREFIX)
    PbConsoleConfig getPbAdminConfig() {
        return new PbConsoleConfig();
    }


    /**
     * 注册全局过滤器
     *
     * @return /
     */
    @Bean
    @Order(PbConstants.ASSEMBLY_ORDER - 1)
    PbServletFilter getPbServletFilterForAdmin() {
        return new PbServletFilter()

                // 拦截路由
                .addInclude("/**")

                // 排除掉登录相关接口，不需要鉴权的
                .addExclude("/favicon.ico", "/console/**")
                //Admin 登录接口
                .addExclude("/api/admins/auth-with-password")

                // 认证函数: 每次请求执行
                .setAuth(obj -> {
                    PbRouter
                            .match(PbConsoleManager.getConfig().getInclude().split(","))
                            .notMatch(PbConsoleManager.getConfig().getExclude().split(","))
                            .check(r -> {
                                // 未登录时直接转发到login页面
                                if (PbConsoleManager.getConfig().getAuth() // 配置文件中配置需要鉴权
                                        && !isSomeOneLogin()
                                ) {
                                    // PbHolder.getRequest().forward("/console/login");
                                    PbHolder.getResponse().redirect("/console/");
                                    PbRouter.back();
                                }

                            });
                }).

                // 异常处理函数：每次认证函数发生异常时执行此函数
                        setError(e -> e.getMessage());
    }

    private boolean isSomeOneLogin() {
        return PbUtil.isLogin() || PbAdminUtil.isLogin();
    }




}
