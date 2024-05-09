package com.picobase.console.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SchemaDeserializer extends JsonDeserializer<Schema> {
    @Override
    public Schema deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        Schema schema = new Schema();

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        List<SchemaField> fields = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode fieldNode : node) {

                SchemaField field = new SchemaField();
                field.setId(fieldNode.get("id").asText());
                field.setName(fieldNode.get("name").asText());
                field.setType(fieldNode.get("type").asText());
                field.setPresentable(fieldNode.get("presentable").asBoolean());
                field.setRequired(fieldNode.get("required").asBoolean());
                field.setSystem(fieldNode.get("system").asBoolean());
                field.setOptions(fieldNode.get("options").asText());
                //数据库反序列化时 直接初始化 options
                //field.initOptions();

                fields.add(field);

            }
        }
        schema.setFields(fields);
        return schema;
    }
}
