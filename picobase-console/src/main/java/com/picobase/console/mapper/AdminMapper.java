package com.picobase.console.mapper;

import com.picobase.PbUtil;
import com.picobase.model.AdminModel;
import com.picobase.persistence.dbx.Expression;
import com.picobase.persistence.dbx.Query;
import com.picobase.persistence.dbx.SelectQuery;
import com.picobase.persistence.model.MapperContext;
import com.picobase.util.PbConstants;

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
                .where(Expression.newExpr("email=:email",
                        context.getWhereParameters()));
    }

    public SelectQuery findAdminById(MapperContext context) {
        return modelQuery().where(Expression.newExpr("id=:id", context.getWhereParameters()));
    }

    public Query deleteAdmin(MapperContext context){
        return PbUtil.getPbDbxBuilder().delete(getTableName(),Expression.newHashExpr(context.getWhereParameters()));
    }


}
