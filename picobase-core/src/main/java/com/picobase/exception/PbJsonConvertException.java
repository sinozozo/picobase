package com.picobase.exception;

/**
 * 一个异常：代表 JSON 转换失败
 */
public class PbJsonConvertException extends PbException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 6806129545290134144L;

    /**
     * 一个异常：代表 JSON 转换失败
     *
     * @param cause 异常对象
     */
    public PbJsonConvertException(Throwable cause) {
        super(cause);
    }

}
