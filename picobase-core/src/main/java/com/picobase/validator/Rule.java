package com.picobase.validator;

/**
 * Rule represents a validation rule.
 */
public interface Rule {
    Err validate(Object value);

}
