package com.picobase.model;

import cn.hutool.json.JSONUtil;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;
import com.picobase.model.schema.fieldoptions.FileOptions;
import com.picobase.model.schema.fieldoptions.RelationOptions;
import com.picobase.model.schema.fieldoptions.SelectOptions;
import com.picobase.util.PbConstants;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecordTest {

    //TODO 文档中追加使用说明
    @Test
    public void testRecordReplaceModifiers() throws JSONException {
        CollectionModel collection = new CollectionModel();
        Schema schema = Schema.newSchema();

        List<SchemaField> fields = schema.getFields();
        fields.add(new SchemaField("text", PbConstants.FieldType.Text));
        fields.add(new SchemaField("number", PbConstants.FieldType.Number));
        fields.add(new SchemaField("rel_one", PbConstants.FieldType.Relation).setOptions(new RelationOptions().setMaxSelect(1)));
        fields.add(new SchemaField("rel_many", PbConstants.FieldType.Relation));
        fields.add(new SchemaField("select_one", PbConstants.FieldType.Select).setOptions(new SelectOptions().setMaxSelect(1)));
        fields.add(new SchemaField("select_many", PbConstants.FieldType.Select).setOptions(new SelectOptions().setMaxSelect(10)));
        fields.add(new SchemaField("file_one", PbConstants.FieldType.File).setOptions(new FileOptions().setMaxSelect(1)));
        fields.add(new SchemaField("file_one_index", PbConstants.FieldType.File).setOptions(new FileOptions().setMaxSelect(1)));
        fields.add(new SchemaField("file_one_name", PbConstants.FieldType.File).setOptions(new FileOptions().setMaxSelect(1)));
        fields.add(new SchemaField("file_many", PbConstants.FieldType.File).setOptions(new FileOptions().setMaxSelect(10)));

        collection.setSchema(schema);

        RecordModel record = new RecordModel(collection);
        Map<String, Object> data = new HashMap<>();
        data.put("text", "test");
        data.put("number", 10);
        data.put("rel_one", "a");
        data.put("rel_many", List.of("a", "b"));
        data.put("select_one", "a");
        data.put("select_many", List.of("a", "b", "c"));
        data.put("file_one", "a");
        data.put("file_one_index", "b");
        data.put("file_one_name", "c");
        data.put("file_many", List.of("a", "b", "c", "d", "e", "f"));
        record.load(data);

        Map<String, Object> map = new HashMap<>();
        map.put("text-", "m-");
        map.put("text+", "m+");
        map.put("number-", 3);
        map.put("number+", 5);
        map.put("rel_one-", "a");
        map.put("rel_one+", "b");
        map.put("rel_many-", List.of("a"));
        map.put("rel_many+", List.of("c", "d", "e"));
        map.put("select_one-", "a");
        map.put("select_one+", "c");
        map.put("select_many-", List.of("b", "c"));
        map.put("select_many+", List.of("d", "e"));
        map.put("file_one+", "skip");//忽略
        map.put("file_one-", "a");
        map.put("file_one_index.0", "");
        map.put("file_one_name.c", "");
        map.put("file_many+", List.of("e", "f"));
        map.put("file_many-", List.of("c", "d"));
        map.put("file_many.f", null);
        map.put("file_many.0", null);

        Map<String, Object> result = record.replaceModifers(map);

        var raw = JSONUtil.toJsonStr(result);

        JSONAssert.assertEquals("""
                {"file_one_name":"","file_many":["b","e"],"file_one":"","number":12,"rel_many":["b","c","d","e"],"select_one":"c","file_one_index":"","select_many":["a","d","e"],"text":"test","rel_one":"b"}
                """, raw, true);
        System.out.println(raw);

    }
}
