package com.picobase.exception;

/**
 * 一个异常：代表请求 path 无效或非法
 */
public class RequestPathInvalidException extends PbException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 8243974276159004739L;

    /**
     * 具体无效的 path
     */
    private final String path;

    /**
     * @return 具体无效的 path
     */
    public String getPath() {
        return path;
    }

    public RequestPathInvalidException(String message, String path) {
        super(message);
        this.path = path;
    }

}
