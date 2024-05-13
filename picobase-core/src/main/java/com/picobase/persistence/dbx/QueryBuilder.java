package com.picobase.persistence.dbx;

import com.picobase.persistence.dbx.expression.Expression;

import java.util.List;
import java.util.Map;

public interface QueryBuilder {
    String buildSelect(List<String> selects, boolean distinct, String selectOption);

    String buildFrom(List<String> from);

    String buildJoin(List<JoinInfo> join, Map<String, Object> params);

    String buildWhere(Expression where, Map<String, Object> params);


    String buildGroupBy(List<String> groupBy);

    String buildHaving(Expression having, Map<String, Object> params);

    String buildOrderByAndLimit(String string, List<String> orderBy, long limit, long offset);

    String buildUnion(List<UnionInfo> union, Map<String, Object> params);

}
