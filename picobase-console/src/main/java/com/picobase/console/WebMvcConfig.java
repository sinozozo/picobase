package com.picobase.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picobase.console.web.mixin.AdminModelMixIn;
import com.picobase.model.AdminModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(customObjectMapper());
        converters.add(converter);
        super.configureMessageConverters(converters);
    }

    @Bean
    public ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 在这里配置你需要的ObjectMapper属性
        objectMapper.addMixIn(AdminModel.class, AdminModelMixIn.class);
        return objectMapper;
    }
}