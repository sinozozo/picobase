package com.picobase.model.schema;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.validator.Err;
import com.picobase.validator.Validatable;
import com.picobase.validator.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.picobase.util.PbConstants.FIELD_VALUE_MODIFIER_ADD;
import static com.picobase.util.PbConstants.FIELD_VALUE_MODIFIER_SUBTRACT;
import static com.picobase.validator.Err.newError;
import static com.picobase.validator.Err.newErrors;
import static com.picobase.validator.Validation.by;


public class Schema implements Validatable {
    private List<SchemaField> fields = new ArrayList<>();

    private Schema() {

    }

    public static Schema newSchema() {
        return new Schema();
    }

    public static Schema newSchema(List<SchemaField> fields) {
        var schema = new Schema();
        for (SchemaField f : fields) {
            schema.addField(f);
        }
        return schema;
    }

    public static List<String> fieldValueModifiers() {
        return List.of(FIELD_VALUE_MODIFIER_ADD, FIELD_VALUE_MODIFIER_SUBTRACT);
    }

    public Schema clone() {
        var schema = new Schema();
        schema.fields.addAll(this.fields);
        return schema;
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

    public List<SchemaField> getFields() {
        return fields;
    }

    public Schema setFields(List<SchemaField> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * 实现了 Validatable 接口，用于嵌套校验，针对持有该对象的父对象校验过程中，如果包含了对该值的校验，最后也会触发该对象的自身校验。即本方法的校验。
     *
     * @return 校验错误信息
     */
    @Override
    public Err validate() {

        /**
         * 这里的返回结构类似
         *   "schema": {
         *             "1": {
         *                 "name": {
         *                     "code": "validation_duplicated_field_name",
         *                     "message": "Duplicated or invalid schema field name."
         *                 }
         *             }
         *         }
         *
         *         schema下一级没有 fields ， 所以这里直接校验值得到校验结果（并没有使用validateObject方法进行校验）
         */
        return Validation.validate(this.fields, by(value -> {

            List<SchemaField> fields = this.fields;
            List<String> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();


            for (int i = 0; i < fields.size(); i++) {
                SchemaField field = fields.get(i);

                if (CollUtil.contains(ids, field.getId())) {
                    return newErrors().put(Integer.toString(i), newErrors().put("id", newError("validation_duplicated_field_id", "Duplicated or invalid schema field id")));
                }

                // field names are used as db columns and should be case insensitive
                String nameLower = field.getName().toLowerCase();

                if (CollUtil.contains(names, nameLower)) {
                    return newErrors().put(Integer.toString(i), newErrors().put("name", newError("validation_duplicated_field_name", "Duplicated or invalid schema field name")));
                }

                ids.add(field.getId());
                names.add(nameLower);
            }

            return null;
        }));
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Schema schema = (Schema) obj;
        return fields.size() == schema.fields.size() && areFieldsEqual(schema);
    }

    private boolean areFieldsEqual(Schema schema) {
        // Sort the fields lists
        List<SchemaField> thisSortedFields = fields.stream().sorted().collect(Collectors.toList());
        List<SchemaField> otherSortedFields = schema.fields.stream().sorted().collect(Collectors.toList());

        return thisSortedFields.equals(otherSortedFields);

    }


}
