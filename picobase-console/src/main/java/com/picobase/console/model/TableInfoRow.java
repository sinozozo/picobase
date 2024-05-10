package com.picobase.pocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * the `db:"pk"` tag has special semantic so we cannot rename
 * the original field without specifying a custom mapper
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
