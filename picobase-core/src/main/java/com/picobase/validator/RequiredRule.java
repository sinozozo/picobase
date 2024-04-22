package com.picobase.validator;

import static com.picobase.validator.Error.newError;

/**
 * RequiredRule is a rule that checks if a value is not empty.
 */
public class RequiredRule implements Rule {

    /**
     * ErrRequired is the error that returns when a value is required.
     */
    public static final Error ErrRequired = newError("validation_required", "cannot be blank");
    /**
     * ErrNilOrNotEmpty is the error that returns when a value is not nil and is empty.
     */
    public static final Error ErrNilOrNotEmpty = newError("validation_nil_or_not_empty_required", "cannot be blank");

    private boolean skipNull;
    private boolean condition;
    private Error err;

    public RequiredRule(boolean condition, boolean skipNull) {
        this.condition = condition;
        this.skipNull = skipNull;
    }

    @Override
    public Error validate(Object value) {
        if (condition) {
            Object checkedValue = Util.indirect(value);
            if ((skipNull && null != checkedValue && Util.isEmpty(checkedValue))
                    || (!skipNull && (null == checkedValue || Util.isEmpty(checkedValue)))) {
                if (err != null) {
                    return err;
                }
                if (skipNull) {
                    return ErrNilOrNotEmpty;
                } else {
                    return ErrRequired;
                }
            }
        }
        return null;
    }

    public RequiredRule when(boolean condition) {
        this.condition = condition;
        return this;
    }

    public RequiredRule error(String message) {
        if (err == null) {
            err = this.skipNull ? ErrNilOrNotEmpty : ErrRequired;
        }
        err.setMessage(message);
        return this;
    }


}
