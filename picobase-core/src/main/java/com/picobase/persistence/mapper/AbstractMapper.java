package com.picobase.persistence.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.persistence.dbx.Query;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.repository.PbRowMapper;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * The abstract mapper contains CRUD methods.
 **/

public abstract class AbstractMapper<T> implements PbMapper {

 /*   private final Class<T> clazz;
    public AbstractMapper(Class<T> clazz) {
        this.clazz = clazz;
       *//* ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.clazz = (Class<T>) genericSuperclass.getActualTypeArguments()[0];*//*
    }

    public Class<T> getModelClass(){
        return this.clazz;
    }*/

    public PbRowMapper<T> getPbRowMapper() {
        return PbManager.getPbRowMapperFactory().getPbRowMapper(getModelClass());
    }


    public SelectQuery modelQuery() {
        var tableName = getTableName();
        return PbUtil.getPbDbxBuilder().select(tableName + ".*").from(tableName);
    }

    @Override
    public SelectQuery findBy(Expression expression) {
        return modelQuery().where(expression);
    }


    @Override
    public Query insertQuery(Object data, String... includeFields) {

        Editor<String> keyEditor = null;
        if (ArrayUtil.isNotEmpty(includeFields)) {
            final Set<String> propertiesSet = CollUtil.set(false, includeFields);
            keyEditor = property -> propertiesSet.contains(property) ? property : null;
        }
        return insertQuery(data, UpsertOptions.create(ArrayUtil.isEmpty(includeFields)).setFieldNameEditor(keyEditor));
    }

    @Override
    public Query insertQuery(Object data, UpsertOptions options) {
        return PbUtil.getPbDbxBuilder().insert(getTableName(), BeanUtil.beanToMap(data, new LinkedHashMap<>(), toCopyOptions(options)));
    }

    @Override
    public Query delete(Expression where) {
        return PbUtil.getPbDbxBuilder().delete(getTableName(), where);
    }

    @Override
    public Query updateQuery(Object data, Expression where, String... includeFields) {

        Editor<String> keyEditor = null;
        if (ArrayUtil.isNotEmpty(includeFields)) {
            final Set<String> propertiesSet = CollUtil.set(false, includeFields);
            keyEditor = property -> propertiesSet.contains(property) ? property : null;
        }
        return updateQuery(data, where, UpsertOptions.create(ArrayUtil.isEmpty(includeFields)).setFieldNameEditor(keyEditor));
    }

    @Override
    public Query updateQuery(Object data, Expression where, UpsertOptions options) {
        return PbUtil.getPbDbxBuilder().update(getTableName(), BeanUtil.beanToMap(data, new LinkedHashMap<>(), toCopyOptions(options)), where);
    }

    /**
     * 转化为 hutool beanToMap 配置项
     *
     * @param options
     * @return
     */
    private CopyOptions toCopyOptions(UpsertOptions options) {
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreNullValue(options.ignoreNullValue);
        copyOptions.setFieldNameEditor(string -> options.fieldNameEditor.edit(string));
        copyOptions.setFieldValueEditor(options.fieldValueEditor);
        copyOptions.setIgnoreProperties(options.ignoreFields);
        return copyOptions;
    }


}
