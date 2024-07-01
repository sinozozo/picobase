package com.picobase.console.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.picobase.annotation.PbCollection;
import com.picobase.event.PbEvent;
import com.picobase.model.BaseModel;

import java.util.Map;

@PbCollection("pb_log")
public class LogModel extends BaseModel implements PbEvent {
    private Map<String, Object> data;
    private String message;
    private int level;

    @JsonIgnore
    private long rowid; //用于数据排序 ， 对应 pocketbase 中的 sqlite rowid 字段


    public Map<String, Object> getData() {
        return data;
    }

    public LogModel setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public LogModel setMessage(String message) {
        this.message = message;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public LogModel setLevel(int level) {
        this.level = level;
        return this;
    }

    public long getRowid() {
        return rowid;
    }

    public LogModel setRowid(long rowid) {
        this.rowid = rowid;
        return this;
    }

    @Override
    public String tableName() {
        return "pb_log";
    }
}
