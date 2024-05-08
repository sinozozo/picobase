package com.picobase.console;

import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.console.config.PbConsoleConfig;
import com.picobase.console.mapper.MapperManagerWithProxy;
import com.picobase.console.web.AdminController;
import com.picobase.console.web.ConsoleController;
import com.picobase.console.web.PbConsoleExceptionHandler;
import com.picobase.console.web.SettingsController;
import com.picobase.jwt.PbAuthZLogicJwtForStateless;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.persistence.mapper.PbMapperManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

@Configuration
@Import({AdminController.class,
        ConsoleController.class,
        SettingsController.class,
        PbConsoleRegister.class,
        PbConsoleExceptionHandler.class,
        WebMvcConfig.class})
public class PbConsoleInject {
    @Autowired(required = false)
    public void setPbAdminConfig(PbConsoleConfig config) {
        PbConsoleManager.setConfig(config);
    }


    @PostConstruct
    public void injectPbAuthZLogic(){
        //为普通 user、pbAdmin 用户注入 jwt token实现 ，最终会由 autoconfig 模块注入， 在PbManager中会根据 type 进行 PbAuthZLogic对象的缓存
        PbUtil.setPbAuthZLogic(new PbAuthZLogicJwtForStateless());

        PbAdminUtil.setPbAuthZLogic(new PbAuthZLogicJwtForStateless("pbAdmin"));

    }


    /**
     * 具有代理能力的 MapperManager ， 用于查看方法调用的sql日志输出
     *
     * PbMapperManager 已经在 autoconfig 模块注入过，这里会覆盖 autoconfig 模块中的注入
     * @return
     */
    @Bean
    @Primary
    public PbMapperManager getPbMapperManager(){
        MapperManagerWithProxy mapperManagerWithProxy = new MapperManagerWithProxy();
        mapperManagerWithProxy.loadInitial();
        return mapperManagerWithProxy;
    }



}
