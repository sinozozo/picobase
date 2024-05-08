package com.picobase.exception;

public class AssertException extends PbException{
    public AssertException(int code) {
        super(code);
    }

    public AssertException(String message) {
        super(message);
    }

    public AssertException(String template, Object... args) {
        super(template, args);
    }

    public AssertException(int code, String message) {
        super(code, message);
    }

    public AssertException(Throwable cause) {
        super(cause);
    }

    public AssertException(String message, Throwable cause) {
        super(message, cause);
    }
}
