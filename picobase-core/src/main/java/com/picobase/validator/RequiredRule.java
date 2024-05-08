package com.picobase.validator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import static com.picobase.validator.Err.newError;

/**
 * RequiredRule is a rule that checks if a value is not empty.
 */
public class RequiredRule implements Rule {

    /**
     * ErrRequired is the error that returns when a value is required.
     */
    public static final Err ErrRequired = newError("validation_required", "cannot be blank");

    private boolean condition = true ;
    private Err err;


    @Override
    public Err validate(Object value) {
        if (condition) {
            if (StrUtil.isEmptyIfStr(value)) {
                return ErrRequired;
            } else if(value instanceof Collection c && CollUtil.isEmpty(c)) {
                return ErrRequired;
            }else if(value instanceof Map m && MapUtil.isEmpty(m)) {
                return ErrRequired;
            }else if(value.getClass().isArray() && Array.getLength(value) == 0) {
                return ErrRequired;
            }
        }
        return null;
    }

    public RequiredRule when(boolean condition) {
        this.condition = condition;
        return this;
    }

    public RequiredRule error(String message) {
        err.setMessage(message);
        return this;
    }


}
