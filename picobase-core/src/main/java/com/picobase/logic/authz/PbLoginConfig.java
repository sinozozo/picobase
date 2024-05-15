package com.picobase.logic.authz;

import java.util.Map;

/**
 * 快速、简洁的构建：调用 `StpUtil.login()` 时的 [ 配置参数 PbLoginModel ]
 *
 * <pre>
 *     	// 例如：在登录时指定 token 有效期为七天，代码如下：
 *     	PbUtil.login(10001, PbLoginConfig.setTimeout(60 * 60 * 24 * 7));
 *
 *     	// 上面的代码与下面的代码等价
 *     	PbUtil.login(10001, new PbLoginConfig().setTimeout(60 * 60 * 24 * 7));
 * </pre>
 */
public class PbLoginConfig {

    private PbLoginConfig() {
    }

    /**
     * @param device 此次登录的客户端设备类型
     * @return 登录参数 Model
     */
    public static PbLoginModel setDevice(String device) {
        return create().setDevice(device);
    }

    /**
     * @param isLastingCookie 是否为持久Cookie（临时Cookie在浏览器关闭时会自动删除，持久Cookie在重新打开后依然存在）
     * @return 登录参数 Model
     */
    public static PbLoginModel setIsLastingCookie(Boolean isLastingCookie) {
        return create().setIsLastingCookie(isLastingCookie);
    }

    /**
     * @param timeout 指定此次登录token的有效期, 单位:秒 （如未指定，自动取全局配置的timeout值）
     * @return 登录参数 Model
     */
    public static PbLoginModel setTimeout(long timeout) {
        return create().setTimeout(timeout);
    }

    /**
     * @param activeTimeout 指定此次登录 token 最低活跃频率，单位：秒（如未指定，自动取全局配置的 activeTimeout 值）
     * @return 对象自身
     */
    public static PbLoginModel setActiveTimeout(long activeTimeout) {
        return create().setActiveTimeout(activeTimeout);
    }

    /**
     * @param extraData 扩展信息（只在jwt模式下生效）
     * @return 登录参数 Model
     */
    public static PbLoginModel setExtraData(Map<String, Object> extraData) {
        return create().setExtraData(extraData);
    }

    /**
     * @param token 预定Token（预定本次登录生成的Token值）
     * @return 登录参数 Model
     */
    public static PbLoginModel setToken(String token) {
        return create().setToken(token);
    }

    /**
     * 写入扩展数据（只在jwt模式下生效）
     *
     * @param key   键
     * @param value 值
     * @return 登录参数 Model
     */
    public static PbLoginModel setExtra(String key, Object value) {
        return create().setExtra(key, value);
    }

    /**
     * @param isWriteHeader 是否在登录后将 Token 写入到响应头
     * @return 登录参数 Model
     */
    public static PbLoginModel setIsWriteHeader(Boolean isWriteHeader) {
        return create().setIsWriteHeader(isWriteHeader);
    }

    /**
     * 设置 本次登录挂载到 TokenSign 的数据
     *
     * @param tokenSignTag /
     * @return 登录参数 Model
     */
    public static PbLoginModel setTokenSignTag(Object tokenSignTag) {
        return create().setTokenSignTag(tokenSignTag);
    }

    /**
     * 静态方法获取一个 PbLoginModel 对象
     *
     * @return PbLoginModel 对象
     */
    public static PbLoginModel create() {
        return new PbLoginModel();
    }

}
