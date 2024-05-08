package com.picobase.exception;

/**
 * 一个异常：代表当前不是 Web 上下文，无法调用某个 API
 */
public class NotWebContextException extends PbException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 6806129545290130144L;

    /**
     * 一个异常：代表当前不是 Web 上下文，无法调用某个 API
     *
     * @param message 异常描述
     */
    public NotWebContextException(String message) {
        super(message);
    }

}
