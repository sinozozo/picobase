package com.picobase.model;


import com.picobase.model.schema.SchemaField;

public class QueryField {

    // field is the final resolved field.
    private SchemaField field;

    // collection refers to the original field's collection model.
    // It could be nil if the found query field is not from a collection schema.
    private CollectionModel collection;

    // original is the original found collection field.
    // It could be nil if the found query field is not from a collection schema.
    private SchemaField original;

    public SchemaField getField() {
        return field;
    }

    public QueryField setField(SchemaField field) {
        this.field = field;
        return this;
    }

    public CollectionModel getCollection() {
        return collection;
    }

    public QueryField setCollection(CollectionModel collection) {
        this.collection = collection;
        return this;
    }

    public SchemaField getOriginal() {
        return original;
    }

    public QueryField setOriginal(SchemaField original) {
        this.original = original;
        return this;
    }
}