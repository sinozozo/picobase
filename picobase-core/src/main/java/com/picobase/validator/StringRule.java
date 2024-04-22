package com.picobase.validator;


import java.util.function.Function;

import static com.picobase.validator.Error.newError;

/**
 * StringRule is a rule that checks a string variable using a specified stringValidator.
 */
public class StringRule implements Rule {
    @FunctionalInterface
    public interface StringValidator<S, T> extends Function<S, T> {
    }

    private StringValidator<String, Boolean> validate;

    private Error err;

    public StringRule(StringValidator stringValidator, String message) {
        this.validate = stringValidator;
        this.err = newError("", message);
    }

    public StringRule(StringValidator stringValidator, Error error) {
        this.validate = stringValidator;
        this.err = error;
    }


    public StringRule error(String message) {
        this.err.setMessage(message);
        return this;
    }


    @Override
    public Error validate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            if (this.validate.apply(str)) {
                return null;
            } else {
                return this.err;
            }
        } else {
            return newError("", "must be a valid String Type");
        }
    }

    public StringRule setErr(Error err) {
        this.err = err;
        return this;
    }
}
