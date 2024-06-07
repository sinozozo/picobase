package com.picobase.persistence.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.picobase.PbUtil;
import com.picobase.persistence.dbx.Query;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.dbx.expression.Expression;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * The abstract mapper contains CRUD methods.
 **/

public abstract class AbstractMapper<T> implements PbMapper {


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
        return insertQuery(data, MappingOptions.create(ArrayUtil.isEmpty(includeFields)).setFieldNameEditor(keyEditor));
    }

    @Override
    public Query insertQuery(Object data, MappingOptions options) {
        return PbUtil.getPbDbxBuilder().insert(getTableName(), BeanUtil.beanToMap(data, new LinkedHashMap<>(), options.toCopyOptions()));
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
        return updateQuery(data, where, MappingOptions.create(ArrayUtil.isEmpty(includeFields)).setFieldNameEditor(keyEditor));
    }

    @Override
    public Query updateQuery(Object data, Expression where, MappingOptions options) {
        return PbUtil.getPbDbxBuilder().update(getTableName(), BeanUtil.beanToMap(data, new LinkedHashMap<>(), options.toCopyOptions()), where);
    }


}
