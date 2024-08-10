package com.picobase.console;

import com.picobase.PbUtil;
import com.picobase.console.config.PbConsoleConfig;
import com.picobase.console.filesystem.LocalFileSystem;
import com.picobase.console.filesystem.S3FileSystem;
import com.picobase.console.repository.PbDatabaseOperateWithLogProxy;
import com.picobase.console.web.LogFilter;
import com.picobase.context.PbHolder;
import com.picobase.file.PbFileSystem;
import com.picobase.filter.PbServletFilter;
import com.picobase.json.PbJsonTemplate;
import com.picobase.logic.PbAdminUtil;
import com.picobase.persistence.repository.PbDatabaseOperate;
import com.picobase.router.PbHttpMethod;
import com.picobase.router.PbRouter;
import com.picobase.util.PbConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

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
                .addExclude("/api/collections/*/auth-with-password")
                // 前置函数：在每次认证函数之前执行
                .setBeforeAuth(obj -> {
                    PbHolder.getResponse()

                            // ---------- 设置跨域响应头 ----------
                            // 允许指定域访问跨域资源
                            .setHeader("Access-Control-Allow-Origin", "*")
                            // 允许所有请求方式
                            .setHeader("Access-Control-Allow-Methods", "*")
                            // 允许的header参数
                            .setHeader("Access-Control-Allow-Headers", "*")
                            // 有效时间
                            .setHeader("Access-Control-Max-Age", "3600")
                    ;

                    // 如果是预检请求，则立即返回到前端
                    PbRouter.match(PbHttpMethod.OPTIONS)
                            .free(r -> System.out.println("--------OPTIONS预检请求，不做处理"))
                            .back();
                })


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


    /**
     * 用于记录日志的过滤器
     *
     * @return LogFilter
     */
    @Bean
    @ConditionalOnProperty(CONFIG_PREFIX + ".isDev")
    LogFilter getLogFilter() {
        return new LogFilter();
    }

    public static boolean isSomeOneLogin() {
        return PbUtil.isLogin() || PbAdminUtil.isLogin();
    }


    @Bean
    public PbFileSystem pbFileSystem(PbConsoleConfig config, PbJsonTemplate jsonTemplate) throws Exception {
        PbFileSystem fileSystem;
        if (doesNotExistClass("com.amazonaws.services.s3.AmazonS3") || config.getS3Config() == null) {
            fileSystem = new LocalFileSystem(config, jsonTemplate);
        } else {
            fileSystem = new S3FileSystem(config);
        }
        fileSystem.init();


        return fileSystem;
    }

    public static boolean doesNotExistClass(String name) {
        try {
            Class.forName(name);
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    /**
     * 具有代理能力的 PbDatabaseOperate ， 用于查看方法调用的sql日志输出
     * <p>
     * PbDatabaseOperate 已经在 autoconfig 模块注入过，这里会覆盖 autoconfig 模块中的注入
     *
     * @return
     */
    @Bean
    @Primary
    @ConditionalOnProperty(CONFIG_PREFIX + ".isDev")
    public PbDatabaseOperate getPbDatabaseOperate(PbConsoleConfig config, JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate) {
        return new PbDatabaseOperateWithLogProxy().create(jdbcTemplate, namedParameterJdbcTemplate, transactionTemplate);
    }


    /**
     * 初始化Pb数据库系统表
     */
    @Bean
    public DatabaseInitializer getDatabaseInitializer() {
        return new DatabaseInitializer();
    }
}
