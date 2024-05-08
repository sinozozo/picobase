package com.picobase.validator;

import static com.picobase.validator.Err.newError;

/**
 * // NotIn returns a validation rule that checks if a value is absent from the given list of values.
 * // Note that the value being checked and the possible range of values must be of the same type.
 * // An empty value is considered valid. Use the Required rule to make sure a value is not empty.
 */
public class NotInRule implements Rule {

    /**
     * // ErrNotInInvalid is the error that returns when a value is in a list.
     */
    private static final Err ErrDateInvalid = newError("validation_date_invalid", "must be a valid date");

    private Object[] elements;
    private Err err;


    public NotInRule(Object... elements) {
        this.elements = elements;
        this.err = ErrDateInvalid;
    }


    @Override
    public Err validate(Object value) {
        Object checkedValue = Util.indirect(value);
        if (null == checkedValue || Util.isEmpty(checkedValue)) {
            return null;
        }
        for (Object e : elements) {
            if (e.equals(value)) {
                return err;
            }
        }
        return null;
    }
}
