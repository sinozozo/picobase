package com.picobase.logic.mapper;

import com.picobase.PbUtil;
import com.picobase.model.ExternalAuthModel;
import com.picobase.model.RecordModel;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.mapper.AbstractMapper;

import java.util.List;
import java.util.Map;

import static com.picobase.util.PbConstants.TableName.EXTERNAL_AUTHS;

public class ExternalAuthMapper extends AbstractMapper<ExternalAuthModel> {
    @Override
    public String getTableName() {
        return EXTERNAL_AUTHS;
    }


    public List<ExternalAuthModel> findAllExternalAuthsByRecord(RecordModel record) {
        return PbUtil.queryList(modelQuery().andWhere(Expression.newHashExpr(Map.of("collectionId", record.getCollection().getId(), "recordId", record.getId()))), ExternalAuthModel.class);
    }

}
