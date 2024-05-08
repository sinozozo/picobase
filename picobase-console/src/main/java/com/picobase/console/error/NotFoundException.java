package com.picobase.console.error;

import com.picobase.exception.PbException;
import com.picobase.validator.Errors;

import static com.picobase.console.error.PbConsoleErrorCode.CODE_404;

public class NotFoundException extends PbException {


    @Override
    public int getCode() {
        if (super.getCode()==0){
            return CODE_404;
        }
        return super.getCode();
    }


    public NotFoundException(){
        super("NotFoundException");
    }

    public NotFoundException(String message) {
        super(message);
    }


}
