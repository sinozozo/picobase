package com.picobase.spring.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.picobase.error.PbSpringBootErrorCode;
import com.picobase.exception.PbJsonConvertException;
import com.picobase.json.PbJsonTemplate;

import java.util.Map;

/**
 * JSON 转换器， Jackson 版实现, 启用字节码增强
 */
public class PbJsonTemplateForJacksonTurbo implements PbJsonTemplate {


    private ObjectMapper objectMapper = new ObjectMapper();

    public PbJsonTemplateForJacksonTurbo() {
        objectMapper.registerModule(new AfterburnerModule());
    }


    /**
     * 将任意对象转换为 json 字符串
     */
    @Override
    public String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new PbJsonConvertException(e).setCode(PbSpringBootErrorCode.CODE_20103);
        }
    }

    /**
     * 将 json 字符串解析为 Map
     */
    @Override
    public Map<String, Object> parseJsonToMap(String jsonStr) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(jsonStr, Map.class);
            return map;
        } catch (JsonProcessingException e) {
            throw new PbJsonConvertException(e).setCode(PbSpringBootErrorCode.CODE_20104);
        }
    }

    /**
     * json 转换为 对象
     *
     * @param jsonStr json str
     * @param clazz   bean class
     * @param <T>     对象类型
     * @return
     */
    @Override
    public <T> T parseJsonToObject(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            throw new PbJsonConvertException(e).setCode(PbSpringBootErrorCode.CODE_20104);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
