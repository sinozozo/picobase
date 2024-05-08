package com.picobase.validator;


import java.util.Arrays;
import java.util.Objects;

import static com.picobase.validator.Err.newError;

/**
 * InRule is a validation rule that validates if a value can be found in the given list of values.
 */
public class InRule implements Rule {

    /**
     * ErrInInvalid is the error that returns in case of an invalid value for "in" rule.
     */
    private final Err ErrInInvalid = newError("validation_in_invalid", "must be a valid value");

    private Object[] elements;

    private Err err = ErrInInvalid;

    public InRule(Object... values) {
        this.elements = values;
    }

    @Override
    public Err validate(Object value) {
        if (value == null) {
            return null;
        }
        if (Arrays.stream(elements).filter(Objects::nonNull).anyMatch(element -> element.equals(value))) {
            return null;
        }
        return this.err;
    }

    public Err errorMessage(String msg) {
        this.err.setMessage(msg);
        return err;
    }

    public InRule setErr(Err err) {
        this.err = err;
        return this;
    }
}
