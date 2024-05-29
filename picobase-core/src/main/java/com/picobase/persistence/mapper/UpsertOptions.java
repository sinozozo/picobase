package com.picobase.persistence.mapper;


import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Insert Update 操作选项<br>
 * 包括：<br>
 * 1、是否忽略空值，当源对象的值为null时，true: 忽略而不注入到sql语句中，false: 注入到sql中<br>
 * 2、忽略的属性列表，设置一个属性列表，sql中不构造该属性<br>
 */
public class UpsertOptions {
    /**
     * 是否忽略空值，当源对象的值为null时，true: 忽略而不注入到sql语句中，false: 注入到sql中
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

    /**
     * 创建Upsert选项
     *
     * @return Upsert选项
     */
    public static UpsertOptions create() {
        return new UpsertOptions();
    }

    /**
     * 创建拷贝选项
     *
     * @param ignoreNullValue  是否忽略空值，当源对象的值为null时，true: 忽略而不注入到sql语句中，false: 注入到sql中
     * @param ignoreProperties 忽略的属性列表，设置一个属性列表，不拷贝这些属性值
     * @return 拷贝选项
     */
    public static UpsertOptions create(boolean ignoreNullValue, String... ignoreProperties) {
        return new UpsertOptions(ignoreNullValue, ignoreProperties);
    }

    public UpsertOptions() {

    }

    /**
     * 构造拷贝选项
     *
     * @param ignoreNullValue  是否忽略空值，当源对象的值为null时，true: 忽略而不注入到sql语句中，false: 注入到sql中
     * @param ignoreProperties 忽略的目标对象中属性列表，设置一个属性列表，sql中不会构造该properties
     */
    public UpsertOptions(boolean ignoreNullValue, String... ignoreProperties) {
        this.ignoreNullValue = ignoreNullValue;
        this.setIgnoreProperties(ignoreProperties);
    }

    /**
     * 设置字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等<br>
     * 此转换器只针对字段做转换，请确认转换后与数据库字段一致<br>
     * 当转换后的字段名为null时忽略这个字段<br>
     *
     * @param fieldNameEditor 字段属性编辑器，用于自定义属性转换规则，例如驼峰转下划线等
     * @return UpsertOptions
     */
    public UpsertOptions setFieldNameEditor(Editor<String> fieldNameEditor) {
        this.fieldNameEditor = fieldNameEditor;
        return this;
    }

    /**
     * 设置是否忽略空值，当源对象的值为null时，true: 忽略而不注入到sql语句中，false: 注入到sql中
     *
     * @param ignoreNullVall 是否忽略空值，当源对象的值为null时，true: 忽略而不注入到sql语句中，false: 注入到sql中
     * @return UpsertOptions
     */
    public UpsertOptions setIgnoreNullValue(boolean ignoreNullVall) {
        this.ignoreNullValue = ignoreNullVall;
        return this;
    }

    /**
     * 设置忽略空值，当源对象的值为null时，true: 忽略而不注入到sql语句中，false: 注入到sql中
     *
     * @return UpsertOptions
     */
    public UpsertOptions ignoreNullValue() {
        return setIgnoreNullValue(true);
    }


    /**
     * 设置忽略的目标对象中属性列表，设置一个属性列表，不在sql中构造这些值
     *
     * @param ignoreProperties 忽略的目标对象中属性列表，设置一个属性列表，不在sql中构造这些值
     * @return UpsertOptions
     */
    public UpsertOptions setIgnoreProperties(String... ignoreProperties) {
        this.ignoreFields = ignoreProperties;
        return this;
    }

    /**
     * 设置忽略的目标对象中属性列表，设置一个属性列表，不在sql中构造这些值，Lambda方式
     *
     * @param <P>   参数类型
     * @param <R>   返回值类型
     * @param funcs 忽略的目标对象中属性列表，设置一个属性列表，不在sql中构造这些值
     * @return UpsertOptions
     */
    @SuppressWarnings("unchecked")
    public <P, R> UpsertOptions setIgnoreProperties(Func1<P, R>... funcs) {
        this.ignoreFields = Arrays.stream(funcs).map(LambdaUtil::getFieldName).toArray(String[]::new);
        return this;
    }

    /**
     * 设置字段属性值编辑器，用于自定义属性值转换规则，例如null转""等<br>
     *
     * @param fieldValueEditor 字段属性值编辑器，用于自定义属性值转换规则，例如null转""等
     * @return UpsertOptions
     */
    public UpsertOptions setFieldValueEditor(BiFunction<String, Object, Object> fieldValueEditor) {
        this.fieldValueEditor = fieldValueEditor;
        return this;
    }

}


