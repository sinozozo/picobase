package com.picobase.exception;

import static com.picobase.error.PbErrorCode.CODE_403;

public class ForbiddenException extends PbException {


    public ForbiddenException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        if (super.getCode() == 0) {
            return CODE_403;
        }
        return super.getCode();
    }
}
