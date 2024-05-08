package com.picobase.spring;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.picobase.PbManager;
import com.picobase.config.PbConfig;
import com.picobase.event.PbEventBus;
import com.picobase.event.PbEventRegisterProcessor;
import com.picobase.json.PbJsonTemplate;
import com.picobase.persistence.dbx.MysqlPbDbxBuilder;
import com.picobase.persistence.dbx.PbDbxBuilder;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.PbDatabaseOperate;
import com.picobase.spring.context.path.ApplicationContextPathLoading;
import com.picobase.spring.json.PbJsonTemplateForJackson;
import com.picobase.spring.json.PbJsonTemplateForJacksonTurbo;
import com.picobase.spring.repository.MysqlDatabaseOperateImpl;
import javassist.ClassPool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * 注册 PicoBase 所需要的Bean
 * <p> Bean 的注册与注入应该分开在两个文件中，否则在某些场景下会造成循环依赖 </p>
 */
public class PbBeanRegister implements ApplicationListener<ApplicationContextEvent> {


    /**
     * 获取配置Bean
     *
     * @return 配置对象
     */
    @Bean
    @ConfigurationProperties(prefix = "picobase")
    public PbConfig getPbConfig() {
        return new PbConfig();
    }

    /**
     * 获取 json 转换器 Bean (Jackson版)
     *
     * @return json 转换器 Bean (Jackson版)
     */
    @Bean
    @ConditionalOnClass(AfterburnerModule.class)
    public PbJsonTemplate getPbJsonTemplateForJacksonTurbo() {
        return new PbJsonTemplateForJacksonTurbo();
    }

    @Bean
    @ConditionalOnMissingClass("com.fasterxml.jackson.module.afterburner.AfterburnerModule")
    public PbJsonTemplate getPbJsonTemplateForJackson() {
        return new PbJsonTemplateForJackson();
    }

    /**
     * 应用上下文路径加载器
     *
     * @return /
     */
    @Bean
    public ApplicationContextPathLoading getApplicationContextPathLoading() {
        return new ApplicationContextPathLoading();
    }

    @Bean
    public PbEventBus getPbEventBus() {
        return new PbEventBus();
    }

    @Bean
    @ConditionalOnMissingClass("javassist.ClassPool")
    public PbEventRegisterProcessor getEventRegisterProcessor(PbEventBus eventBus) {
        PbManager.getLog().warn("PbEvent 没有找到 javassist 依赖,将采用反射方式执行EventReceiver");
        return new PbEventRegisterProcessor(eventBus, false);
    }

    @Bean("eventRegisterProcessor")
    @ConditionalOnClass(ClassPool.class)
    public PbEventRegisterProcessor getEventRegisterProcessor2(PbEventBus eventBus) {
        return new PbEventRegisterProcessor(eventBus, true);
    }

    @Bean
    public PbDatabaseOperate getPbDatabaseOperate(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate, PbMapperManager mapperManager) {
        return new MysqlDatabaseOperateImpl(jdbcTemplate,namedParameterJdbcTemplate, transactionTemplate,mapperManager);
    }

    @Bean
    public PbMapperManager getMapperManager(PbConfig pbConfig) {
        PbMapperManager pbMapperManager = new PbMapperManager();
        //加载 mapper
        pbMapperManager.loadInitial();
        return pbMapperManager;
    }

    @Bean
    public PbDbxBuilder getPbDbxBuilder(PbDatabaseOperate operate) {
        return new MysqlPbDbxBuilder(operate);
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {

        if (event instanceof ContextRefreshedEvent) { //当 Spring 容器初始化完成后
            if (PbManager.getPbEventRegisterProcessor()==null) {
                return;
            }
            // 获取所有 bean 的名称
            String[] beanNames = event.getApplicationContext().getBeanDefinitionNames();
            // 遍历所有 bean
            for (String beanName : beanNames) {
                Object bean = event.getApplicationContext().getBean(beanName);
                PbManager.getPbEventRegisterProcessor().postProcessAfterInitialization(bean);
            }

        } else if (event instanceof ContextClosedEvent) {
            if (PbManager.getPbEventBus() != null) {
                //优雅停机
                PbManager.getPbEventBus().destroy();
                //spring 应用一般会自动关掉ForkJoinPool线程池
                shutdownForkJoinPool();
            }
        }
    }

    private static void shutdownForkJoinPool() {
        try {
            ForkJoinPool.commonPool().shutdown();

            if (ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS)) {
                ForkJoinPool.commonPool().shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



}
