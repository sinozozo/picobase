package com.picobase.console.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import com.picobase.PbUtil;
import com.picobase.model.AdminModel;
import com.picobase.persistence.dbx.Expression;
import com.picobase.persistence.dbx.Query;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.model.MapperContext;
import com.picobase.persistence.resolver.ListUtil;
import com.picobase.util.PbConstants;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.picobase.persistence.dbx.Expression.newExpr;

public class AdminMapper extends AbstractBeanPropertyRowMapper<AdminModel> {


    @Override
    public String getTableName() {
        return PbConstants.TableName.ADMIN;
    }

    @Override
    public  Class<AdminModel> getModelClass() {
        return AdminModel.class;
    }


    public SelectQuery modelQuery() {
        var tableName = getTableName();
        return PbUtil.getPbDbxBuilder().select(tableName + ".*").from(tableName);
    }

    public SelectQuery findAdminByEmail(MapperContext context) {
        return modelQuery()
                .where(newExpr("email=:email",
                        context.getWhereParameters()));
    }

    public SelectQuery findAdminById(MapperContext context) {
        return modelQuery().where(newExpr("id=:id", context.getWhereParameters()));
    }

    public Query deleteAdmin(MapperContext context){
        return PbUtil.getPbDbxBuilder().delete(getTableName(),Expression.newHashExpr(context.getWhereParameters()));
    }

    public SelectQuery isAdminEmailUnique(String email ,String ... excludeIds) {
        // 过滤掉null值并转换为Set确保唯一性
        Set<String> uniqueIds = Arrays.stream(excludeIds)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        SelectQuery sq = modelQuery().select("count(*)").where(newExpr("email=:email",
                Map.of("email", email))).limit(1);

        if(uniqueIds.size() > 0){
            sq.andWhere(Expression.notIn("id",uniqueIds));
        }

        return sq;
    }


    public Query saveAdmin(AdminModel admin) {
        Map<String, Object> adminMap = BeanUtil.beanToMap(admin);
        return PbUtil.getPbDbxBuilder().insert(getTableName(),adminMap);
    }

    public Query updateAdmin(AdminModel admin) {
        Map<String, Object> adminMap = BeanUtil.beanToMap(admin);
        return PbUtil.getPbDbxBuilder().update(getTableName(),adminMap,newExpr("id=:id", Map.of("id", admin.getId())));
    }
}
