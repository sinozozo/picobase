package com.picobase.basic;

/**
 * Http Basic 认证模块，Util 工具类
 */
public class PbBasicUtil {

    private PbBasicUtil() {
    }

    /**
     * 底层使用的 PbBasicTemplate 对象
     */
    public static PbBasicTemplate saBasicTemplate = new PbBasicTemplate();

    /**
     * 获取浏览器提交的 Basic 参数 （裁剪掉前缀并解码）
     *
     * @return 值
     */
    public static String getAuthorizationValue() {
        return saBasicTemplate.getAuthorizationValue();
    }

    /**
     * 对当前会话进行 Basic 校验（使用全局配置的账号密码），校验不通过则抛出异常
     */
    public static void check() {
        saBasicTemplate.check();
    }

    /**
     * 对当前会话进行 Basic 校验（手动设置账号密码），校验不通过则抛出异常
     *
     * @param account 账号（格式为 user:password）
     */
    public static void check(String account) {
        saBasicTemplate.check(account);
    }

    /**
     * 对当前会话进行 Basic 校验（手动设置 Realm 和 账号密码），校验不通过则抛出异常
     *
     * @param realm   领域
     * @param account 账号（格式为 user:password）
     */
    public static void check(String realm, String account) {
        saBasicTemplate.check(realm, account);
    }

}
