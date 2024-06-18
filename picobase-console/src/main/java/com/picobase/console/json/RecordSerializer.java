package com.picobase.console.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.picobase.model.RecordModel;

import java.io.IOException;
import java.util.Map;

public class RecordSerializer extends JsonSerializer<RecordModel> {


    @Override
    public void serialize(RecordModel record, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // record 未被导出过
        if (!record.isAlreadyExported()) {
            //执行 map 构建 ，导出数据
            record.publicExport();
        }
        // 获取要导出的数据
        Map<String, Object> exportedData = record.getPublicData();
        //执行序列化
        gen.writeObject(exportedData);
    }
}
