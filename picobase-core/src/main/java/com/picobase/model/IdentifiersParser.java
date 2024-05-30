package com.picobase.model;

import java.util.List;

public class IdentifiersParser {
    private List<Identifier> columns;
    private List<Identifier> tables;

    public IdentifiersParser(List<Identifier> columns, List<Identifier> tables) {
        this.columns = columns;
        this.tables = tables;
    }

    public List<Identifier> getColumns() {
        return columns;
    }

    public List<Identifier> getTables() {
        return tables;
    }
}