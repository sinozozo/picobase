package com.picobase.exception;

/**
 * 一个异常：代表 Type 转换失败
 */
public class PbTypeConvertException extends PbException {

    /**
     * 一个异常：代表 Type 转换失败
     *
     * @param cause 异常对象
     */
    public PbTypeConvertException(Throwable cause) {
        super(cause);
    }

}
