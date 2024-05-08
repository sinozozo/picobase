package com.picobase.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Errors extends HashMap<String, Err> implements Err {

    /**
     * Err returns the error string of Errors.
     */
    @Override
    public String error() {
        if (isEmpty()) {
            return "";
        }

        SortedSet<String> keys = new TreeSet<>(keySet());
        StringBuilder sb = new StringBuilder();

        for (String key : keys) {
            Err err = get(key);
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(key).append(": ").append(err instanceof Errors ? "(" + ((Errors) err).error() + ")" : err.error());
        }

        sb.append(".");
        return sb.toString();
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

    /**
     * Filter removes all nils from Errors and returns back the updated Errors as an error.
     * If the length of Errors becomes 0, it will return nil.
     */
    public Errors filter() {
        entrySet().removeIf(entry -> entry.getValue() == null);
        return isEmpty() ? null : this;
    }
}
