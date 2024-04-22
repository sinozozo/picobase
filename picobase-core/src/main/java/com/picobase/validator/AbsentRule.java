package com.picobase.validator;


import static com.picobase.validator.Error.newError;

public class AbsentRule implements Rule {

    /**
     * ErrNil is the error that returns when a value is not nil.
     */
    private static final Error ErrNil = newError("validation_nil", "must be blank");

    /**
     * ErrEmpty is the error that returns when a not nil value is not empty.
     */
    private static final Error ErrEmpty = newError("validation_empty", "must be blank");

    private boolean condition;
    private Error err;
    private boolean skipNull;

    public AbsentRule(boolean condition, boolean skipNull) {
        this.condition = condition;
        this.skipNull = skipNull;
    }


    @Override
    public Error validate(Object value) {
        if (condition) {
            Object checkedValue = Util.indirect(value);
            if (!this.skipNull && null != checkedValue || this.skipNull && null != checkedValue && !Util.isEmpty(checkedValue)) {
                if (err != null) {
                    return err;
                }
                if (this.skipNull) {
                    return ErrEmpty;
                }
                return ErrNil;
            }
        }
        return null;
    }

    public AbsentRule when(boolean condition) {
        this.condition = condition;
        return this;
    }

    public AbsentRule error(String message) {
        if (err == null) {
            err = this.skipNull ? ErrEmpty : ErrNil;
        }
        err.setMessage(message);
        return this;
    }
}
