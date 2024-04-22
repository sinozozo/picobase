package com.picobase.validator;

import java.util.Map;

/**
 * Error interface represents an validation error.
 */
public interface Error {
    String error();

    String code();

    String message();

    Error setMessage(String message);

    Map<String, Object> params();

    Error setParams(Map<String, Object> params);

    /**
     * NewError create new validation error.
     */
    static Error newError(String code, String message) {
        return new ErrorObject(code, message);
    }
}
