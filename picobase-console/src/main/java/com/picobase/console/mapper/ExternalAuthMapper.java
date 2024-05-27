package com.picobase.console.mapper;

import com.picobase.PbUtil;
import com.picobase.model.ExternalAuthModel;
import com.picobase.model.RecordModel;
import com.picobase.persistence.dbx.expression.Expression;

import java.util.List;
import java.util.Map;

import static com.picobase.util.PbConstants.TableName.EXTERNAL_AUTHS;

public class ExternalAuthMapper extends AbstractBeanPropertyRowMapper<ExternalAuthModel> {
    @Override
    public String getTableName() {
        return EXTERNAL_AUTHS;
    }

    @Override
    public Class<ExternalAuthModel> getModelClass() {
        return ExternalAuthModel.class;
    }


    public List<ExternalAuthModel> findAllExternalAuthsByRecord(RecordModel record) {
        return PbUtil.query(modelQuery().andWhere(Expression.newHashExpr(Map.of("collectionId", record.getCollection().getId(), "recordId", record.getId()))), ExternalAuthModel.class);
    }

}
