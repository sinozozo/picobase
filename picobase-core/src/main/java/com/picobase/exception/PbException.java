package com.picobase.exception;

import com.picobase.error.PbErrorCode;
import com.picobase.util.PbInnerUtil;

/**
 * Pb 框架内部逻辑发生错误抛出的异常
 *
 * <p> 框架其它异常均继承自此类，开发者可通过捕获此异常来捕获框架内部抛出的所有异常 </p>
 */
public class PbException extends RuntimeException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 6806129545290130132L;

    /**
     * 异常细分状态码
     */
    private int code = PbErrorCode.CODE_UNDEFINED;

    /**
     * 构建一个异常
     *
     * @param code 异常细分状态码
     */
    public PbException(int code) {
        super();
        this.code = code;
    }


    /**
     * 构建一个异常
     *
     * @param message 异常描述信息
     */
    public PbException(String message) {
        super(message);
    }

    /**
     * 构建一个异常
     *
     * @param code    异常细分状态码
     * @param message 异常信息
     */
    public PbException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构建一个异常
     *
     * @param cause 异常对象
     */
    public PbException(Throwable cause) {
        super(cause);
    }

    /**
     * 构建一个异常
     *
     * @param message 异常信息
     * @param cause   异常对象
     */
    public PbException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 获取异常细分状态码
     *
     * @return 异常细分状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 写入异常细分状态码
     *
     * @param code 异常细分状态码
     * @return 对象自身
     */
    public PbException setCode(int code) {
        this.code = code;
        return this;
    }

    /**
     * 如果flag==true，则抛出message异常
     *
     * @param flag    标记
     * @param message 异常信息
     * @param code    异常细分状态码
     */
    public static void throwBy(boolean flag, String message, int code) {
        if (flag) {
            throw new PbException(message).setCode(code);
        }
    }

    /**
     * 如果value==null或者isEmpty，则抛出message异常
     *
     * @param value   值
     * @param message 异常信息
     * @param code    异常细分状态码
     */
    public static void throwByNull(Object value, String message, int code) {
        if (PbInnerUtil.isEmpty(value)) {
            throw new PbException(message).setCode(code);
        }
    }

}
