package com.picobase.persistence.mapper;


import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.bean.copier.IJSONTypeConverter;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.TypeUtil;
import com.picobase.PbManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * rQuery Insert Update 操作选项<br>
 * 包括：<br>
 * 1、是否忽略空值，当源对象的值为null时，true: 忽略而不做处理（rQuery null字段不做映射 ，upsert 操作不注入到sql语句中） ，false: 同理取反<br>
 * 2、忽略的属性列表，设置一个忽略的属性列表，（rQuery 该集合字段不做映射 ，upsert 操作不注入到sql语句中）<br>
 */
public class MappingOptions {

    private final Pattern jsonPattern = Pattern.compile("^(?:\\{|\\[).*?(?:\\}|\\])$");

    /**
     * 是否忽略空值，当源对象的值为null时，true: 忽略而不做处理（rQuery null字段不做映射 ，upsert 操作不注入到sql语句中） ，false: 同理取反<br>
     */
    protected boolean ignoreNullValue;

    /**
     * 字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等<br>
     * 规则为，{@link Editor#edit(Object)}属性为源对象的字段名称或key，返回值为目标对象的字段名称或key
     */
    protected Editor<String> fieldNameEditor;

    /**
     * 字段属性值编辑器，用于自定义属性值转换规则，例如null转""等
     */
    protected BiFunction<String, Object, Object> fieldValueEditor;

    /**
     * sql 忽略字段
     */
    protected String[] ignoreFields;

    protected TypeConverter converter = (type, value) -> {
        if (null == value) {
            return null;
        }

        if (value instanceof IJSONTypeConverter) {
            return ((IJSONTypeConverter) value).toBean(ObjectUtil.defaultIfNull(type, Object.class));
        }

        // type 为 map 或 集合类型 且 value 为json 字符串的情况
        Class c = TypeUtil.getClass(type);
        if (Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c)) {
            if (value instanceof String json && maybeIsValidJsonString(json)) {
                return PbManager.getPbJsonTemplate().parseJsonToObject(json, c);
            }
        }


        return Convert.convertWithCheck(type, value, null, true);
    };

    public MappingOptions setConverter(TypeConverter converter) {
        this.converter = converter;
        return this;
    }

    private boolean maybeIsValidJsonString(String jsonString) {

        Matcher matcher = jsonPattern.matcher(jsonString);

        return matcher.matches();
    }

    public MappingOptions() {

    }

    /**
     * 构造拷贝选项
     *
     * @param ignoreNullValue  是否忽略空值，当源对象的值为null时，true: 忽略而不做处理（rQuery null字段不做映射 ，upsert 操作不注入到sql语句中） ，false: 同理取反<br>
     * @param ignoreProperties 忽略的目标对象中属性列表，设置一个属性列表，rQuery中不会映射该属性， upsert中不注入到sql语句中
     */
    public MappingOptions(boolean ignoreNullValue, String... ignoreProperties) {
        this.ignoreNullValue = ignoreNullValue;
        this.setIgnoreProperties(ignoreProperties);
    }

    /**
     * 创建Upsert选项
     *
     * @return Upsert选项
     */
    public static MappingOptions create() {
        return new MappingOptions();
    }

    /**
     * 创建拷贝选项
     *
     * @param ignoreNullValue  是否忽略空值，当源对象的值为null时，true: 忽略而不做处理（rQuery null字段不做映射 ，upsert 操作不注入到sql语句中） ，false: 同理取反<br>
     * @param ignoreProperties 忽略的目标对象中属性列表，设置一个属性列表，rQuery中不会映射该属性， upsert中不注入到sql语句中
     * @return 拷贝选项
     */
    public static MappingOptions create(boolean ignoreNullValue, String... ignoreProperties) {
        return new MappingOptions(ignoreNullValue, ignoreProperties);
    }

    /**
     * 设置字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等<br>
     * 此转换器只针对字段做转换，请确认转换后与数据库字段一致<br>
     * 当转换后的字段名为null时忽略这个字段<br>
     *
     * @param fieldNameEditor 字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等
     * @return MappingOptions
     */
    public MappingOptions setFieldNameEditor(Editor<String> fieldNameEditor) {
        this.fieldNameEditor = fieldNameEditor;
        return this;
    }

    /**
     * 设置是否忽略空值，当源对象的值为null时，true: 忽略而不做处理（rQuery null字段不做映射 ，upsert 操作不注入到sql语句中） ，false: 同理取反<br>
     *
     * @param ignoreNullVall 是否忽略空值，当源对象的值为null时，true: 忽略而不做处理（rQuery null字段不做映射 ，upsert 操作不注入到sql语句中） ，false: 同理取反<br>
     * @return MappingOptions
     */
    public MappingOptions setIgnoreNullValue(boolean ignoreNullVall) {
        this.ignoreNullValue = ignoreNullVall;
        return this;
    }

    /**
     * 是否忽略空值，当源对象的值为null时，true: 忽略而不做处理（rQuery null字段不做映射 ，upsert 操作不注入到sql语句中） ，false: 同理取反<br>
     *
     * @return MappingOptions
     */
    public MappingOptions ignoreNullValue() {
        return setIgnoreNullValue(true);
    }


    /**
     * 设置忽略的目标对象中属性列表，设置一个属性列表，rQuery中不会映射该属性， upsert中不注入到sql语句中
     *
     * @param ignoreProperties 忽略的目标对象中属性列表，设置一个属性列表，rQuery中不会映射该属性， upsert中不注入到sql语句中
     * @return MappingOptions
     */
    public MappingOptions setIgnoreProperties(String... ignoreProperties) {
        this.ignoreFields = ignoreProperties;
        return this;
    }

    /**
     * 设置忽略的目标对象中属性列表，设置一个属性列表，rQuery中不会映射该属性， upsert中不注入到sql语句中，Lambda方式
     *
     * @param <P>   参数类型
     * @param <R>   返回值类型
     * @param funcs 忽略的目标对象中属性列表，设置一个属性列表，rQuery中不会映射该属性， upsert中不注入到sql语句中
     * @return MappingOptions
     */
    @SuppressWarnings("unchecked")
    public <P, R> MappingOptions setIgnoreProperties(Func1<P, R>... funcs) {
        this.ignoreFields = Arrays.stream(funcs).map(LambdaUtil::getFieldName).toArray(String[]::new);
        return this;
    }

    /**
     * 设置字段属性值编辑器，用于自定义属性值转换规则，例如null转""等<br>
     *
     * @param fieldValueEditor 字段属性值编辑器，用于自定义属性值转换规则，例如null转""等
     * @return MappingOptions
     */
    public MappingOptions setFieldValueEditor(BiFunction<String, Object, Object> fieldValueEditor) {
        this.fieldValueEditor = fieldValueEditor;
        return this;
    }


    /**
     * 转化为 hutool beanToMap 配置项
     */
    public CopyOptions toCopyOptions() {
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setConverter(this.converter);
        copyOptions.setIgnoreNullValue(this.ignoreNullValue);
        if (this.fieldNameEditor != null) {
            copyOptions.setFieldNameEditor(string -> this.fieldNameEditor.edit(string));
        }
        copyOptions.setFieldValueEditor(this.fieldValueEditor);
        copyOptions.setIgnoreProperties(this.ignoreFields);
        return copyOptions;
    }

}


