package com.picobase.validator;

import cn.hutool.core.util.StrUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * ErrObject is the default validation error
 * that implements the Err interface.
 */
public class ErrObject implements Err {
    private String code;
    private String message;
    private Map<String, Object> params;

    public ErrObject() {

    }

    public ErrObject(String code, String message) {
        this.code = code;
        this.message = message;
        this.params = new HashMap<>();
    }

    /**
     * Err returns the error message.
     */
    @Override
    public String error() {
        if (params == null || params.isEmpty()) {
            return message;
        }
        return StrUtil.format(this.message, this.params);
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
    public Err setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Map<String, Object> params() {
        return this.params;
    }

    @Override
    public Err setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public String getCode() {
        return code;
    }

    public ErrObject setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    
}
