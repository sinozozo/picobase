package com.picobase.model;


public class FailureResult {
    private int code;
    private String message;
    private Object data;

    public int getCode() {
        return code;
    }

    public FailureResult setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public FailureResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public FailureResult setData(Object data) {
        this.data = data;
        return this;
    }

}
