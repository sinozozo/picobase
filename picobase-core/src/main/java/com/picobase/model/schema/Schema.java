package com.picobase.model.schema;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.RandomUtil;
import com.picobase.PbManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.picobase.util.PbConstants.FIELD_VALUE_MODIFIER_ADD;
import static com.picobase.util.PbConstants.FIELD_VALUE_MODIFIER_SUBTRACT;


public class Schema {
    private List<SchemaField> fields = new ArrayList<>();


    public Schema() {
    }


    public void addField(SchemaField newField) {
        if (StrUtil.isEmpty(newField.getId())) {
            // set default id
            newField.setId(RandomUtil.randomString(8));
        }

        for (int i = 0; i < fields.size(); i++) {
            SchemaField field = fields.get(i);
            // replace existing
            if (field.getId().equals(newField.getId())) {
                fields.set(i, newField);
                return;
            }
        }

        // add new field
        fields.add(newField);
    }


    // GetFieldById returns a single field by its id.
    public SchemaField getFieldById(String id) {
        return fields.stream().filter(field -> field.getId().equals(id)).findFirst().orElse(null);
    }

    public SchemaField getFieldByName(String fieldName) {
        return fields.stream().filter(field -> field.getName().equals(fieldName)).findFirst().orElse(null);
    }

    public Map<String, SchemaField> asMap() {
        return fields.stream().collect(Collectors.toMap(SchemaField::getName, field -> field));
    }


    public String toJson() {
        return PbManager.getPbJsonTemplate().toJsonString(this);
    }

    public static List<String> fieldValueModifiers() {
        return List.of(FIELD_VALUE_MODIFIER_ADD, FIELD_VALUE_MODIFIER_SUBTRACT);
    }

    public List<SchemaField> getFields() {
        return fields;
    }

    public Schema setFields(List<SchemaField> fields) {
        this.fields = fields;
        return this;
    }
}
