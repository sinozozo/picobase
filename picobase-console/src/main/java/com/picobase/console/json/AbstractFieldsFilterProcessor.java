package com.picobase.console.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.picobase.context.PbHolder;
import com.picobase.context.model.PbRequest;
import com.picobase.logic.FieldsFilterProcessor;

import java.io.IOException;
import java.util.Map;

import static com.picobase.util.PbConstants.QueryParam.FIELDS;

/**
 * 针对 fileds filter 封装处理
 *
 * @param <T>
 */
public abstract class AbstractFieldsFilterProcessor<T> extends JsonSerializer<T> {


    public void serialize(T t, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Map<String, Object> exportedData = convertBeanToExportedMap(t);

        PbRequest request = PbHolder.getRequest();
        String fields = request.getParameter(FIELDS);

        if (!StrUtil.isEmpty(fields)) {
            //根据fields 清洗待导出的Map
            FieldsFilterProcessor.pickFields(exportedData, fields);
        }

        //执行序列化
        gen.writeObject(exportedData);
    }

    /**
     * 子类实现 bean 转换为Map ， 最终会导出这个map 作为json响应
     *
     * @param t
     * @return
     */
    public abstract Map<String, Object> convertBeanToExportedMap(T t);


}
