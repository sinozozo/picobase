package com.picobase.validator;

import java.util.Map;

/**
 * Err interface represents an validation error.
 */
public interface Err {
    String error();

    String code();

    String message();

    Err setMessage(String message);

    Map<String, Object> params();

    Err setParams(Map<String, Object> params);

    /**
     * NewError create new validation error.
     */
    static Err newError(String code, String message) {
        return new ErrObject(code, message);
    }

    static Errors newErrors() {
        return new Errors();
    }
}
