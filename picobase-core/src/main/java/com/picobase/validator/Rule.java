package com.picobase.validator;

/**
 * Rule represents a validation rule.
 */
public interface Rule {
    Error validate(Object value);

}
