package com.picobase.spring;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.picobase.PbManager;
import com.picobase.config.PbConfig;
import com.picobase.event.PbEventBus;
import com.picobase.event.PbEventRegisterProcessor;
import com.picobase.json.PbJsonTemplate;
import com.picobase.persistence.dbx.MysqlPbDbxBuilder;
import com.picobase.persistence.dbx.PbDbxBuilder;
import com.picobase.persistence.mapper.PbMapper;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.PbDatabaseOperate;
import com.picobase.persistence.repository.PbRowMapper;
import com.picobase.persistence.repository.PbRowMapperFactory;
import com.picobase.scheduler.PbSchedulerBus;
import com.picobase.spring.context.path.ApplicationContextPathLoading;
import com.picobase.spring.json.PbJsonTemplateForJackson;
import com.picobase.spring.json.PbJsonTemplateForJacksonTurbo;
import com.picobase.spring.repository.MysqlDatabaseOperateImpl;
import com.picobase.strategy.PbStrategy;
import javassist.ClassPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * 注册 PicoBase 所需要的Bean
 * <p> Bean 的注册与注入应该分开在两个文件中，否则在某些场景下会造成循环依赖 </p>
 */
public class PbBeanRegister {


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
    @ConditionalOnMissingBean(PbDatabaseOperate.class)
    public PbDatabaseOperate getPbDatabaseOperate(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate, TransactionTemplate transactionTemplate) {
        return new MysqlDatabaseOperateImpl(jdbcTemplate, namedParameterJdbcTemplate, transactionTemplate);
    }

    @Bean
    //@ConditionalOnMissingClass("com.picobase.console.mapper.MapperManagerWithProxy")
    public PbMapperManager getMapperManager(PbConfig pbConfig) {
        return PbManager.getPbMapperManager();
    }

    @Bean
    public PbDbxBuilder getPbDbxBuilder(PbDatabaseOperate operate) {
        return new MysqlPbDbxBuilder(operate);
    }


    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ApplicationContextEvent event) {

        if (PbManager.getPbEventRegisterProcessor() == null) {
            return;
        }
        // 处理 eventbus 组件
        // 获取所有 bean 的名称
        String[] beanNames = event.getApplicationContext().getBeanDefinitionNames();
        // 遍历所有 bean
        for (String beanName : beanNames) {
            Object bean = event.getApplicationContext().getBean(beanName);
            PbManager.getPbEventRegisterProcessor().postProcessAfterInitialization(bean);
        }


        // 处理 scheduler 组件

        //配置系统默认 CronExpress 解析测量
        PbStrategy.instance.setNextTimestampByCronExpressionFunction((cron, time) -> CronExpression.parse(cron).next(time).toInstant().toEpochMilli());


        Map<String, Object> beansWithAnnotation = event.getApplicationContext().getBeansWithAnnotation(Component.class);
        beansWithAnnotation.forEach((k, v) -> PbSchedulerBus.inject(v));


        //TODO 启动后检查 PB 数据库，不存在PB 核心表则创建

    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosedEvent() {
        if (PbManager.getPbEventBus() != null) {
            //优雅停机
            PbManager.getPbEventBus().destroy();

            PbSchedulerBus.shutdown();
            //spring 应用一般会自动关掉ForkJoinPool线程池
            shutdownForkJoinPool();
        }
    }

    @Bean
    public PbRowMapperFactory getPbRowMapperFactory() {
        return new PbRowMapperFactory() {
            @Override
            public <T> PbRowMapper<T> getPbRowMapper(Class<T> clazz) {
                return (rs, rowNum) -> {
                    BeanPropertyRowMapper<T> mapper = new BeanPropertyRowMapper<>(clazz);
                    return mapper.mapRow(rs, rowNum);
                };
            }
        };
    }


    /**
     * 注入 PbMapper 到容器中 // TODO 移植到 PbBeanInject 中会报错
     *
     * @param beanFactory
     */
    @Autowired
    public void registerSpiBeans(ConfigurableListableBeanFactory beanFactory) {
        for (PbMapper mapper : PbManager.getPbMapperManager().getAllMappers()) {
            beanFactory.registerSingleton(mapper.getClass().getName(), mapper);
        }
    }

}
