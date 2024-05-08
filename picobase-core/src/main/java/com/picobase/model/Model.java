package com.picobase.model;

import java.time.LocalDateTime;

public interface Model {
    String tableName();

    boolean hasId();

    String getId();

    void setId(String id);

    LocalDateTime getCreated();

    LocalDateTime getUpdated();

    void refreshId();

    void refreshCreated();

    void refreshUpdated();
}