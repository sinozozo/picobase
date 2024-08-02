package com.picobase.console.model;


import com.picobase.model.RecordModel;

public class RecordAuthResponse {
    private String token;
    private RecordModel record;
    private Object meta;

    public RecordAuthResponse(String token, RecordModel record, Object meta) {
        this.token = token;
        this.record = record;
        this.meta = meta;
    }

    public String getToken() {
        return token;
    }

    public RecordAuthResponse setToken(String token) {
        this.token = token;
        return this;
    }

    public RecordModel getRecord() {
        return record;
    }

    public RecordAuthResponse setRecord(RecordModel record) {
        this.record = record;
        return this;
    }

    public Object getMeta() {
        return meta;
    }

    public RecordAuthResponse setMeta(Object meta) {
        this.meta = meta;
        return this;
    }
}
