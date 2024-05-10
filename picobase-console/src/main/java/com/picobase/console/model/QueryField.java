package com.picobase.pocket.model;

import com.picobase.pocket.model.schema.SchemaField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryField {

    // field is the final resolved field.
    private SchemaField field;

    // collection refers to the original field's collection model.
    // It could be nil if the found query field is not from a collection schema.
    private CollectionModel collection;

    // original is the original found collection field.
    // It could be nil if the found query field is not from a collection schema.
    private SchemaField original;

}