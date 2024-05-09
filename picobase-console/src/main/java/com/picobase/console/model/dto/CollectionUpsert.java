package com.picobase.console.model.dto;

import com.picobase.model.schema.Schema;

import java.util.*;


public class CollectionUpsert {

    private String id;
    private String name;
    private String type;
    private boolean system;
    private Schema schema;
    private List<String> indexes;
    private String listRule;
    private String viewRule;
    private String createRule;
    private String updateRule;
    private String deleteRule;
    private Map<String, Object> options;

    public String getId() {
        return id;
    }

    public CollectionUpsert setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CollectionUpsert setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public CollectionUpsert setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isSystem() {
        return system;
    }

    public CollectionUpsert setSystem(boolean system) {
        this.system = system;
        return this;
    }

    public Schema getSchema() {
        return schema;
    }

    public CollectionUpsert setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public CollectionUpsert setIndexes(List<String> indexes) {
        this.indexes = indexes;
        return this;
    }

    public String getListRule() {
        return listRule;
    }

    public CollectionUpsert setListRule(String listRule) {
        this.listRule = listRule;
        return this;
    }

    public String getViewRule() {
        return viewRule;
    }

    public CollectionUpsert setViewRule(String viewRule) {
        this.viewRule = viewRule;
        return this;
    }

    public String getCreateRule() {
        return createRule;
    }

    public CollectionUpsert setCreateRule(String createRule) {
        this.createRule = createRule;
        return this;
    }

    public String getUpdateRule() {
        return updateRule;
    }

    public CollectionUpsert setUpdateRule(String updateRule) {
        this.updateRule = updateRule;
        return this;
    }

    public String getDeleteRule() {
        return deleteRule;
    }

    public CollectionUpsert setDeleteRule(String deleteRule) {
        this.deleteRule = deleteRule;
        return this;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public CollectionUpsert setOptions(Map<String, Object> options) {
        this.options = options;
        return this;
    }
}
