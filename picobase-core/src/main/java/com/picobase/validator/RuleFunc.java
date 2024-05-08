package com.picobase.validator;

/**
 * RuleFunc represents a validator function.
 * You may wrap it as a Rule by calling By().
 */
@FunctionalInterface
public interface RuleFunc {

    Err apply(Object value);
}
