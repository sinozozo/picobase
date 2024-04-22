package com.picobase.validator;

import com.picobase.util.StrFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * ErrorObject is the default validation error
 * that implements the Error interface.
 */
public class ErrorObject implements Error {
    private String code;
    private String message;
    private Map<String, Object> params;


    public ErrorObject(String code, String message) {
        this.code = code;
        this.message = message;
        this.params = new HashMap<>();
    }

    /**
     * Error returns the error message.
     */
    @Override
    public String error() {
        if (params == null || params.isEmpty()) {
            return message;
        }
        return StrFormatter.format(this.message, this.params);
    }

    @Override
    public String code() {
        return this.code;
    }

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public Error setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Map<String, Object> params() {
        return this.params;
    }

    @Override
    public Error setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

}
