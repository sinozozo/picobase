package com.picobase.persistence.resolver;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.stream.Stream;

import static com.picobase.persistence.dbx.DbxUtil.columnify;


public record SimpleFieldResolver(List<String> allowedFields) implements FieldResolver {

    @Override
    public ResolverResult resolve(String field) {
        if (!ListUtil.existInListWithRegex(field, allowedFields)) {
            throw new RuntimeException("Failed to resolve field " + field);
        }


        // var parts = field.split("."); 注意split的方法入参是正则，执行后 parts 为empty.
        List<String> parts = StrUtil.split(field, '.', -1);
        if (parts.size() == 1) {
            return ResolverResult.builder().identifier("`" + columnify(parts.get(0)) + "`").build();
        }


        // treat as json path
        StringBuilder jsonPath = new StringBuilder("$");

        // 使用流 API
        parts.stream().skip(1).forEach(part -> {
            if (NumberUtil.isNumber(part)) {
                jsonPath.append("[").append(columnify(part)).append("]");
            } else {
                jsonPath.append(".").append(part);
            }
        });

        return ResolverResult.builder().noCoalesce(true).identifier(
                String.format(
                        "JSON_EXTRACT(`%s`, '%s')", //TODO 修改为反引号
                        columnify(parts.get(0)),
                        jsonPath)
        ).build();
    }

}
