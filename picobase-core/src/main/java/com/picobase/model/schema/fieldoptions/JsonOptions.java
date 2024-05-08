package com.picobase.model.schema.fieldoptions;


import com.picobase.validator.Errors;

import static com.picobase.validator.Validation.*;

public class JsonOptions implements FieldOptions {

    private int maxSize;


    @Override
    public Errors validate() {
        return validateObject(this,
                field(JsonOptions::getMaxSize, required, min(1)));
    }

    public int getMaxSize() {
        return maxSize;
    }
}
