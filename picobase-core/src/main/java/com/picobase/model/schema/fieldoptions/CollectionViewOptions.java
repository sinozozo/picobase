package com.picobase.model.schema.fieldoptions;


import com.picobase.validator.Errors;
import com.picobase.validator.Validatable;

import static com.picobase.validator.Validation.*;

// CollectionViewOptions defines the "view" Collection.Options fields.@Data
public class CollectionViewOptions implements Validatable {

    private String query;


    @Override
    public Errors validate() {
        return validateObject(this,
                field(CollectionViewOptions::getQuery, required));
    }

    public String getQuery() {
        return query;
    }

    public CollectionViewOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
