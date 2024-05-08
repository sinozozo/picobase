package com.picobase.model.schema.fieldoptions;


import com.picobase.validator.Errors;
import com.picobase.validator.Rule;
import com.picobase.validator.RuleFunc;

import static com.picobase.validator.Err.newError;
import static com.picobase.validator.Validation.*;

public class NumberOptions implements FieldOptions {
    private Integer min;
    private Integer max;
    private boolean noDecimal;


    @Override
    public Errors validate() {
        Rule[] maxRules = new Rule[2];
        if (null != min && null != max) {
            maxRules[0] = min(this.max);
            maxRules[1] = by(this.checkNoDecimal());
        }

        return validateObject(this,
                field(NumberOptions::getMin, by(this.checkNoDecimal())),
                field(NumberOptions::getMax, maxRules)
        );
    }

    private RuleFunc checkNoDecimal() {
        return (Object value) -> {
            Double v = (Double) value;
            if (v == null || !noDecimal) {
                return null; // nothing to check
            }

            if (!v.equals(Math.floor(v))) {
                return newError("validation_no_decimal_constraint", "Decimal numbers are not allowed.");
            }
            return null;
        };
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public boolean isNoDecimal() {
        return noDecimal;
    }
}
