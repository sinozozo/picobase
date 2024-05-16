package com.picobase.console.json;

import com.picobase.model.RecordModel;

import java.util.Map;

public class RecordSerializer extends AbstractFieldsFilterProcessor<RecordModel> {

    @Override
    public Map<String, Object> convertBeanToExportedMap(RecordModel record) {
        // record 未被导出过
        if (!record.isAlreadyExported()) {
            //执行 map 构建 ，导出数据
            record.publicExport();
        }
        // 获取要导出的数据
        Map<String, Object> exportedData = record.getPublicData();
        return exportedData;
    }
}
