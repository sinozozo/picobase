package com.picobase.validator;


public class SkipRule implements Rule {

    private boolean skip;

    public SkipRule when(boolean condition) {
        this.skip = condition;
        return this;
    }


    @Override
    public Err validate(Object value) {
        return null;
    }

    public boolean isSkip() {
        return skip;
    }
}