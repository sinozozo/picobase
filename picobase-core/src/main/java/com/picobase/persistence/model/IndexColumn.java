package com.picobase.persistence.model;



/**
 * IndexColumn represents a single parsed SQL index column.
 */
public class IndexColumn {

    private String name;

    private String collate;

    private String sort;

    public IndexColumn(String name, String collate, String sort) {
        this.name = name;
        this.collate = collate;
        this.sort = sort;
    }

    public IndexColumn() {
    }

    public String getName() {
        return name;
    }

    public IndexColumn setName(String name) {
        this.name = name;
        return this;
    }

    public String getCollate() {
        return collate;
    }

    public IndexColumn setCollate(String collate) {
        this.collate = collate;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public IndexColumn setSort(String sort) {
        this.sort = sort;
        return this;
    }
}
