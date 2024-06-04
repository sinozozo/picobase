package com.picobase.logic.mapper;

import cn.hutool.core.date.DateUtil;
import com.picobase.PbManager;
import com.picobase.model.CollectionModel;
import com.picobase.model.schema.Schema;
import com.picobase.persistence.repository.PbRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CollectionRowMapper implements PbRowMapper<CollectionModel> {
    @Override
    public CollectionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        CollectionModel model = new CollectionModel();
        model.setId(rs.getString("id"));
        model.setName(rs.getString("name"));
        model.setType(rs.getString("type"));
        model.setSystem(rs.getBoolean("system"));

        Schema schema = PbManager.getPbJsonTemplate().parseJsonToObject(rs.getString("schema"), Schema.class);
        model.setSchema(schema);

        List<String> indexList = PbManager.getPbJsonTemplate().parseJsonToObject(rs.getString("indexes"), List.class);
        model.setIndexes(indexList);

        model.setListRule(rs.getString("listRule"));
        model.setCreateRule(rs.getString("createRule"));
        model.setDeleteRule(rs.getString("deleteRule"));
        model.setUpdateRule(rs.getString("updateRule"));
        model.setViewRule(rs.getString("viewRule"));

        Map<String, Object> options = PbManager.getPbJsonTemplate().parseJsonToObject(rs.getString("options"), Map.class);
        model.setOptions(options);

        model.setCreated(DateUtil.toLocalDateTime(rs.getTimestamp("created")));
        model.setUpdated(DateUtil.toLocalDateTime(rs.getTimestamp("updated")));


        return model;
    }
}
