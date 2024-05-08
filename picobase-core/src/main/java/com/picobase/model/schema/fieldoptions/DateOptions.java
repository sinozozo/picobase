package com.picobase.model.schema.fieldoptions;


import com.picobase.validator.Errors;
import com.picobase.validator.RuleFunc;

import java.time.LocalDateTime;

import static com.picobase.util.PbConstants.DefaultDateLayout;
import static com.picobase.validator.Validation.*;


public class DateOptions implements FieldOptions {
    private LocalDateTime min;
    private LocalDateTime max;


    @Override
    public Errors validate() {
        return validateObject(this, field(DateOptions::getMax, by(checkRange(this.min, this.max))));
    }

    private RuleFunc checkRange(LocalDateTime min, LocalDateTime max) {
        return (Object value) -> {
            LocalDateTime v = (LocalDateTime) value;

            if (null == v || null == min || null == max) {
                return null; // nothing to check
            }


            return date(DefaultDateLayout) // TODO 需要验证
                    .min(min.toLocalTime())
                    .max(max.toLocalTime())
                    .validate(v.toString());
        };
    }

    public LocalDateTime getMin() {
        return min;
    }

    public DateOptions setMin(LocalDateTime min) {
        this.min = min;
        return this;
    }

    public LocalDateTime getMax() {
        return max;
    }

    public DateOptions setMax(LocalDateTime max) {
        this.max = max;
        return this;
    }
}
