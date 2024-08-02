package com.picobase.model.schema.fieldoptions;


import com.picobase.validator.Errors;
import com.picobase.validator.Is;

import java.util.List;

import static com.picobase.validator.Validation.*;


public class EmailOptions implements FieldOptions {
    private List<String> exceptDomains;
    private List<String> onlyDomains;


    @Override
    public Errors validate() {
        return validateObject(this,
                field(EmailOptions::getExceptDomains, when(null != onlyDomains && onlyDomains.size() > 0, Empty).otherwise(each(Is.Domain))),
                field(EmailOptions::getOnlyDomains, when(null != exceptDomains && exceptDomains.size() > 0, Empty).otherwise(each(Is.Domain))));
    }

    public List<String> getExceptDomains() {
        return exceptDomains;
    }

    public EmailOptions setExceptDomains(List<String> exceptDomains) {
        this.exceptDomains = exceptDomains;
        return this;
    }

    public List<String> getOnlyDomains() {
        return onlyDomains;
    }

    public EmailOptions setOnlyDomains(List<String> onlyDomains) {
        this.onlyDomains = onlyDomains;
        return this;
    }
}
