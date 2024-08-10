package com.picobase.console;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.picobase.console.json.LocalDateTimeDeserializer;
import com.picobase.console.json.LocalDateTimeSerializer;
import com.picobase.console.json.RecordSerializer;
import com.picobase.console.json.mixin.AdminModelMixIn;
import com.picobase.console.json.mixin.SchemaMixIn;
import com.picobase.console.web.interceptor.LoadCollectionInterceptor;
import com.picobase.context.PbHolder;
import com.picobase.context.model.PbRequest;
import com.picobase.logic.FieldsFilterProcessor;
import com.picobase.model.AdminModel;
import com.picobase.model.RecordModel;
import com.picobase.model.schema.Schema;
import com.picobase.persistence.mapper.PbMapperManager;
import com.picobase.persistence.repository.Page;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.picobase.util.PbConstants.QueryParam.FIELDS;

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
        MappingJackson2HttpMessageConverter converter = new FieldsFilterHttpMessageConverter();

        ObjectMapper objectMapper = new ObjectMapper();

        //配置jackson mixin 不污染影响 pb core中的实体
        objectMapper.addMixIn(AdminModel.class, AdminModelMixIn.class);
        objectMapper.addMixIn(Schema.class, SchemaMixIn.class);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addSerializer(RecordModel.class, new RecordSerializer());
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
     * 增加 LoadCollectionInterceptor 拦截器， 会根据controller handler 注解 LoadCollection条件，向当前Request中注入Collection对象
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // LoadCollection Interceptor finds the collection with related
        registry.addInterceptor(new LoadCollectionInterceptor(mapperManager))
                .addPathPatterns("/api/collections/*/records")
                .addPathPatterns("/api/collections/*/records/*")
                .addPathPatterns("/api/collections/*/auth-with-password")
                .addPathPatterns("/api/files/**");
    }
    
}

class FieldsFilterHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    /**
     * Write the given object to the given message.
     * MappingJackson2HttpMessageConverter 看源代码，存在两个writeInternal方法 ， 要重写带有 Type 参数的 方法， 否则不会被执行
     *
     * @param object        the object to write to the output message
     * @param type          the type of object to write (may be {@code null})
     * @param outputMessage the HTTP output message to write to
     * @throws IOException
     * @throws HttpMessageNotWritableException
     */
    @Override
    protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        PbRequest request = PbHolder.getRequest();
        String fields = request.getParameter(FIELDS);
        if (StrUtil.isEmpty(fields)) { //无需 fields 过滤 ,直接 response
            super.writeInternal(object, null, outputMessage); //super 的调用 必须是 三个参数的 writeInternal ， 否则会递归死循环
            return;
        }

        // 存在 fields 过滤
        ObjectMapper mapper = super.getObjectMapper();
        String encoded = mapper.writeValueAsString(object);
        Map decoded = mapper.readValue(encoded, Map.class);

        if (object instanceof Page) {
            Object list = decoded.get("items");
            FieldsFilterProcessor.pickFields(list, fields);
        } else {
            FieldsFilterProcessor.pickFields(decoded, fields);

        }
        super.writeInternal(decoded, null, outputMessage); //super 的调用 必须是 三个参数的 writeInternal ， 否则会递归死循环

    }


}