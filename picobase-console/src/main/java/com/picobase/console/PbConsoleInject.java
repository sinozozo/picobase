package com.picobase.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.picobase.PbUtil;
import com.picobase.console.config.PbConsoleConfig;
import com.picobase.console.json.LocalDateTimeDeserializer;
import com.picobase.console.json.LocalDateTimeSerializer;
import com.picobase.console.json.SchemaDeserializer;
import com.picobase.console.json.mixin.AdminModelMixIn;
import com.picobase.console.json.mixin.SchemaMixIn;
import com.picobase.console.mapper.MapperManagerWithProxy;
import com.picobase.console.web.AdminController;
import com.picobase.console.web.ConsoleController;
import com.picobase.console.web.PbConsoleExceptionHandler;
import com.picobase.console.web.SettingsController;
import com.picobase.exception.PbException;
import com.picobase.json.PbJsonTemplate;
import com.picobase.jwt.PbAuthZLogicJwtForStateless;
import com.picobase.model.AdminModel;
import com.picobase.model.schema.Schema;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.spring.json.PbJsonTemplateForJackson;
import com.picobase.spring.json.PbJsonTemplateForJacksonTurbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Configuration
@Import({AdminController.class,
        ConsoleController.class,
        SettingsController.class,
        PbConsoleRegister.class,
        PbConsoleExceptionHandler.class,
        WebMvcConfig.class
})
public class PbConsoleInject {
    @Autowired(required = false)
    public void setPbAdminConfig(PbConsoleConfig config) {
        PbConsoleManager.setConfig(config);
    }


    @PostConstruct
    public void injectPbAuthZLogic() {
        //为普通 user、pbAdmin 用户注入 jwt token实现 ，最终会由 autoconfig 模块注入， 在PbManager中会根据 type 进行 PbAuthZLogic对象的缓存
        PbUtil.setPbAuthZLogic(new PbAuthZLogicJwtForStateless());

        PbAdminUtil.setPbAuthZLogic(new PbAuthZLogicJwtForStateless("pbAdmin"));

    }


    /**
     * 具有代理能力的 MapperManager ， 用于查看方法调用的sql日志输出
     * <p>
     * PbMapperManager 已经在 autoconfig 模块注入过，这里会覆盖 autoconfig 模块中的注入
     *
     * @return
     */
    @Bean
    @Primary
    public PbMapperManager getPbMapperManager() {
        MapperManagerWithProxy mapperManagerWithProxy = new MapperManagerWithProxy();
        mapperManagerWithProxy.loadInitial();
        return mapperManagerWithProxy;
    }


    /**
     * 拿出底层 PbJsonTemplate 的 ObjectMapper 进行配置,
     *
     * @param jsonTemplate
     * @return
     */
    @Autowired(required = false)
    public void configPbJsonTemplate(PbJsonTemplate jsonTemplate) {
        ObjectMapper objectMapper;
        if (jsonTemplate instanceof PbJsonTemplateForJackson pj) {
            objectMapper = pj.getObjectMapper();
        } else if (jsonTemplate instanceof PbJsonTemplateForJacksonTurbo pjt) {
            objectMapper = pjt.getObjectMapper();
        } else {
            throw new PbException("jsonTemplate must be PbJsonTemplateForJackson or PbJsonTemplateForJacksonTurbo");
        }

        // 在这里配置你需要的ObjectMapper属性
        objectMapper.addMixIn(AdminModel.class, AdminModelMixIn.class);
        objectMapper.addMixIn(Schema.class, SchemaMixIn.class);


        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Schema.class, new SchemaDeserializer());
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(simpleModule);
    }

}
