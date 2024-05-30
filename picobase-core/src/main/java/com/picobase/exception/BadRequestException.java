package com.picobase.exception;

import com.picobase.validator.Errors;

import static com.picobase.error.PbErrorCode.CODE_400;

/**
 * 一个异常：代表请求参数不合法 默认 code 为 400
 */
public class BadRequestException extends PbException {


    private Errors errors;

    @Override
    public int getCode() {
        if (super.getCode() == 0) {
            return CODE_400;
        }
        return super.getCode();
    }

    public Errors getErrors() {
        return errors;
    }

    public BadRequestException setErrors(Errors errors) {
        this.errors = errors;
        return this;
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Errors errors) {
        super(message);
        this.errors = errors;
    }

    public BadRequestException(Errors errors) {
        super("An error occurred while submitting the form.");
        this.errors = errors;
    }
}
