package com.picobase.error;

/**
 * 定义 picobase-spring-boot-starter 所有异常细分状态码
 */
public interface PbSpringBootErrorCode {

    /**
     * 企图在非 Web 上下文获取 Request、Response 等对象
     */
    int CODE_20101 = 20101;

    /**
     * 对象转 JSON 字符串失败
     */
    int CODE_20103 = 20103;

    /**
     * JSON 字符串转对象 失败
     */
    int CODE_20104 = 20104;

    /**
     * 默认的 Filter 异常处理函数
     */
    int CODE_20105 = 20105;

}
