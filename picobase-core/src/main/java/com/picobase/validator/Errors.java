package com.picobase.validator;

import java.util.HashMap;
import java.util.Map;

public class Errors implements Err {

    private Map<String, Err> errorMap = new HashMap<>();


    public Errors put(String fieldName, Err err) {
        this.errorMap.put(fieldName, err);
        return this;
    }

    public Map<String, Err> getErrorMap() {
        return errorMap;
    }

    /**
     * Filter removes all nils from Errors and returns back the updated Errors as an error.
     * If the length of Errors becomes 0, it will return nil.
     */
    public Errors filter() {
        this.errorMap.entrySet().removeIf(entry -> entry.getValue() == null);
        return errorMap.isEmpty() ? null : this;
    }

    public int size() {
        return this.errorMap.size();
    }

    public boolean isEmpty() {
        return this.errorMap.isEmpty();
    }

    @Override
    public String error() {
        return null;
    }

    @Override
    public String code() {
        return null;
    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public Err setMessage(String message) {
        return null;
    }

    @Override
    public Map<String, Object> params() {
        return null;
    }

    @Override
    public Err setParams(Map<String, Object> params) {
        return null;
    }
}
