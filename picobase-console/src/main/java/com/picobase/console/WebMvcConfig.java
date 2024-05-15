package com.picobase.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.picobase.console.json.LocalDateTimeDeserializer;
import com.picobase.console.json.LocalDateTimeSerializer;
import com.picobase.console.json.mixin.AdminModelMixIn;
import com.picobase.console.json.mixin.SchemaMixIn;
import com.picobase.console.web.interceptor.LoadCollectionInterceptor;
import com.picobase.model.AdminModel;
import com.picobase.model.schema.Schema;
import com.picobase.persistence.mapper.PbMapperManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private PbMapperManager mapperManager;

    /**
     * 交由spring 注入 mapperManager
     *
     * @param mapperManager
     */
    public WebMvcConfig(PbMapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    /**
     * * 配置自定义的HttpMessageConverter 的Bean ，在Spring MVC 里注册HttpMessageConverter有两个方法：
     * * 1、configureMessageConverters ：重载会覆盖掉Spring MVC 默认注册的多个HttpMessageConverter
     * * 2、extendMessageConverters ：仅添加一个自定义的HttpMessageConverter ，不覆盖默认注册的HttpMessageConverter
     *
     * @param converters initially an empty list of converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        ObjectMapper objectMapper = new ObjectMapper();

        //配置jackson mixin 不污染影响 pb core中的实体
        objectMapper.addMixIn(AdminModel.class, AdminModelMixIn.class);
        objectMapper.addMixIn(Schema.class, SchemaMixIn.class);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        objectMapper.registerModule(simpleModule);
        converter.setObjectMapper(objectMapper);
        /**
         * converters 会for循环查找应该由哪一个convert进行处理， 然后调用直接返回， 自定义的converter如果放在最后会得不到执行
         *
         * AbstractMessageConverterMethodProcessor.writeWithMessageConverters
         */
        converters.add(0, converter);
    }

    /**
     * 增加 LoadCollectionInterceptor 拦截器， 会根据controller handler 注解 LoadCollection条件，向当前Reuqest中注入Collection对象
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // LoadCollection Interceptor finds the collection with related
        registry.addInterceptor(new LoadCollectionInterceptor(mapperManager))
                .addPathPatterns("/api/collections/*/records")
                .addPathPatterns("/api/collections/*/records/*")
                .addPathPatterns("/api/collections/*/auth-with-password") // TODO 这里会导致未认证的请求查询数据库，考虑增加Collection缓存
                .addPathPatterns("/api/files/**");
    }
}