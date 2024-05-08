package com.picobase.model.schema.fieldoptions;


import com.picobase.validator.Errors;

public class BoolOptions implements FieldOptions {

    private boolean value;

    @Override
    public Errors validate() {
        return null;
    }

    public boolean isValue() {
        return value;
    }

    public BoolOptions setValue(boolean value) {
        this.value = value;
        return this;
    }
}
