package com.picobase.console.mapper;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.picobase.PbManager;
import com.picobase.console.model.LogModel;
import com.picobase.console.model.LogsStatsItem;
import com.picobase.json.PbJsonTemplate;
import com.picobase.persistence.dbx.expression.Expression;
import com.picobase.persistence.mapper.AbstractMapper;
import com.picobase.persistence.repository.ModifyRequest;

import java.util.List;
import java.util.stream.Collectors;

public class LogMapper extends AbstractMapper<LogModel> {

    private static final String SAVE_LOG_SQL = "insert into pb_log(id,level,message,data,created,updated,rowid) values(?,?,?,?,?,?,?)";


    public void batchSave(List<LogModel> logs) {
        PbJsonTemplate pbJsonTemplate = PbManager.getPbJsonTemplate();
        List<ModifyRequest> requests = logs.stream().map(log -> {
            ModifyRequest request = new ModifyRequest(SAVE_LOG_SQL);
            request.setArgs(new Object[]{log.getId(), log.getLevel(), log.getMessage(), pbJsonTemplate.toJsonString(log.getData()), log.getCreated(), log.getUpdated(), log.getRowid()});
            return request;
        }).collect(Collectors.toList());
        if (requests.isEmpty()) {
            return;
        }
        PbManager.getPbDatabaseOperate().update(requests);
    }

    public List<LogsStatsItem> stats(Expression expr) {
        var query = modelQuery().select("count(id) as total", "DATE_FORMAT(created,'%Y-%m-%d %H:00:00') as date").orderBy("date").groupBy("date");
        if (expr != null) {
            query.andWhere(expr);
        }
        return query.all(LogsStatsItem.class);
    }

    @Override
    public String getTableName() {
        return "pb_log";
    }

    public void deleteBeforeTime(DateTime dateTime) {
        String sql = "delete from pb_log where created <= ?";
        PbManager.getPbDatabaseOperate().update(List.of(new ModifyRequest(sql).setArgs(new Object[]{DateUtil.toLocalDateTime(dateTime)})));
    }
}
