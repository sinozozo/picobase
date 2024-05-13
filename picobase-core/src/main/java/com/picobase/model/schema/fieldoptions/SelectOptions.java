package com.picobase.model.schema.fieldoptions;


import com.picobase.model.schema.MultiValuer;
import com.picobase.validator.Errors;

import java.util.List;

import static com.picobase.validator.Validation.*;

public class SelectOptions implements FieldOptions, MultiValuer {
    private int maxSelect;
    private List<String> values;
    

    //@JsonIgnore
    @Override
    public boolean isMultiple() {
        return this.maxSelect > 1;
    }


    @Override
    public Errors validate() {
        int max = values.size();
        if (max == 0) {
            max = 1;
        }

        return validateObject(this,
                field(SelectOptions::getValues, required),
                field(SelectOptions::getMaxSelect, min(1), max(max))
        );
    }

    public int getMaxSelect() {
        return maxSelect;
    }

    public SelectOptions setMaxSelect(int maxSelect) {
        this.maxSelect = maxSelect;
        return this;
    }

    public List<String> getValues() {
        return values;
    }

    public SelectOptions setValues(List<String> values) {
        this.values = values;
        return this;
    }
}
