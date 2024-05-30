package com.picobase.model;

/**
 * the `db:"pk"` tag has special semantic so we cannot rename
 * the original field without specifying a custom mapper
 */

public class TableInfoRow {

    private int pk;
    private int index;
    private String name;
    private String type;
    private boolean notNull;
    private String defaultValue;


    public TableInfoRow(int pk, String name, String type, boolean notNull, String defaultValue) {
        this.pk = pk;
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.defaultValue = defaultValue;
    }

    public int getPk() {
        return pk;
    }

    public TableInfoRow setPk(int pk) {
        this.pk = pk;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public TableInfoRow setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getName() {
        return name;
    }

    public TableInfoRow setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public TableInfoRow setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public TableInfoRow setNotNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public TableInfoRow setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
}
