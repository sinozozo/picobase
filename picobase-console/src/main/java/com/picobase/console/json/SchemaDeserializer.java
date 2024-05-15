package com.picobase.console.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.picobase.PbManager;
import com.picobase.model.schema.Schema;
import com.picobase.model.schema.SchemaField;

import java.io.IOException;
import java.util.*;

public class SchemaDeserializer extends JsonDeserializer<Schema> {
    @Override
    public Schema deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {


        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        List<SchemaField> fields = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode fieldNode : node) {

                SchemaField field = new SchemaField();

                if (fieldNode.has("id")) {
                    field.setId(fieldNode.get("id").asText());
                }
                if (fieldNode.has("name")) {
                    field.setName(fieldNode.get("name").asText());
                }
                if (fieldNode.has("type")) {
                    field.setType(fieldNode.get("type").asText());
                }
                if (fieldNode.has("presentable")) {
                    field.setPresentable(fieldNode.get("presentable").asBoolean());
                }
                if (fieldNode.has("required")) {
                    field.setRequired(fieldNode.get("required").asBoolean());
                }
                if (fieldNode.has("system")) {
                    field.setSystem(fieldNode.get("system").asBoolean());
                }
                if (fieldNode.has("options")) {
                    if (fieldNode.get("options").isObject()) {
                        // bindReuquest 时，options为ObjectNode
                        field.setOptions(convertObjectNodeToMap((ObjectNode) fieldNode.get("options")));
                    } else {
                        // 数据库中读取时，options为String
                        //这里将options类型转换为Map类型
                        String options = fieldNode.get("options").asText();
                        if (StrUtil.isNotEmpty(options)) {
                            field.setOptions(PbManager.getPbJsonTemplate().parseJsonToObject(options, Map.class));
                        }
                    }
                }

                //数据库反序列化时 直接初始化 options
                //field.initOptions();

                fields.add(field);

            }
        }
        Schema schema = Schema.newSchema(fields);
        return schema;
    }

    private Map<String, Object> convertObjectNodeToMap(ObjectNode objectNode) {
        Map<String, Object> map = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode value = entry.getValue();
            if (value.isObject()) {
                map.put(entry.getKey(), convertObjectNodeToMap((ObjectNode) value));
            } else if (value.isArray()) {
                map.put(entry.getKey(), convertArrayNodeToList((ArrayNode) value));
            } else {
                map.put(entry.getKey(), value.asText());
            }
        }
        return map;
    }

    private List<Object> convertArrayNodeToList(ArrayNode arrayNode) {
        List<Object> list = new ArrayList<>(arrayNode.size());
        arrayNode.forEach(node -> {
            if (node.isObject()) {
                list.add(convertObjectNodeToMap((ObjectNode) node));
            } else if (node.isArray()) {
                list.add(convertArrayNodeToList((ArrayNode) node));
            } else {
                list.add(node.asText());
            }
        });
        return list;
    }
}
