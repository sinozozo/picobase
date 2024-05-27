package com.picobase.model;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;

import static com.picobase.util.PbConstants.DEFAULT_ID_LENGTH;

public abstract class BaseModel implements Model {
    private String id;
    private LocalDateTime created;
    private LocalDateTime updated;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public abstract String tableName();


    public void refreshCreated() {
        created = LocalDateTime.now();
    }

    public void refreshUpdated() {
        updated = LocalDateTime.now();
    }

    public void refreshId() {
        this.id = RandomUtil.randomString(DEFAULT_ID_LENGTH);
    }

    public boolean hasId() {
        return StrUtil.isNotEmpty(id);
    }
}
