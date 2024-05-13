package com.picobase.console.json.mixin;

import com.fasterxml.jackson.annotation.JsonValue;
import com.picobase.model.schema.SchemaField;

import java.util.List;

/**
 * jackson mixin ，用于在不修改原始类的情况下，为类添加或修改序列化和反序列化的规则。
 * <p>
 * PbConsole中会有两套jackson objectmapper 配置规则， 分别用于http响应json序列化 和数据库json序列化
 */
public abstract class SchemaMixIn {

    /**
     * 这里mixin到Schema中，让schema的json中直接输出fields数组内容，而不是直接输出fields对象
     */

    @JsonValue // 直接作为 Schema 对象的 json 值输出
    public abstract List<SchemaField> getFields();

}
