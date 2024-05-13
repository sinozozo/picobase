package com.picobase.validator;

/**
 * Validatable is the interface indicating the type implementing it supports data validation.
 */
public interface Validatable {

    /**
     * Validate validates the data and returns an error if validation fails.
     */
    Err validate();
}