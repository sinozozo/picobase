package com.picobase.logic;

import com.picobase.PbManager;
import com.picobase.listener.PbEventCenter;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.logic.authz.PbLoginModel;
import com.picobase.logic.authz.PbTokenInfo;

import static com.picobase.util.PbConstants.LOGIN_TYPE_ADMIN;

/**
 * 多类型用户下用于区分Admin 和 user ，这里主要提供Admin登录相关接口， 其他能力请使用 PbUtil
 */
public class PbAdminUtil {


    private PbAdminUtil() {
    }


    /**
     * 底层使用的 PbAuthZLogic 对象
     */
    public static PbAuthZLogic pbAzLogic = new PbAuthZLogic(LOGIN_TYPE_ADMIN);


    /**
     * 安全的重置 PbAuthZLogic 对象
     *
     * <br> 1、更改此账户的 PbAuthZLogic 对象
     * <br> 2、put 到全局 PbAuthZLogic 集合中
     * <br> 3、发送日志
     *
     * @param newLogic /
     */
    public static void setPbAuthZLogic(PbAuthZLogic newLogic) {
        // 1、重置此账户的 PbAuthZLogic 对象
        pbAzLogic = newLogic;

        // 2、添加到全局 PbAuthZLogic 集合中
        //    以便可以通过 PbManager.getPbAuthZLogic(type) 的方式来全局获取到这个 PbAuthZLogic
        PbManager.putPbAuthZLogic(newLogic);

        // 3、$$ 发布事件：更新了 pbAzLogic 对象

        PbEventCenter.doSetPbAuthZLogic(pbAzLogic);
    }
    // 会话查询

    /**
     * 判断当前会话是否已经登录
     *
     * @return 已登录返回 true，未登录返回 false
     */
    public static boolean isLogin() {
        return pbAzLogic.isLogin();
    }

    /**
     * 判断指定账号是否已经登录
     *
     * @return 已登录返回 true，未登录返回 false
     */
    public static boolean isLogin(Object loginId) {
        return pbAzLogic.isLogin(loginId);
    }

    // --- 登录

    /**
     * 会话登录
     *
     * @param id 账号id，建议的类型：（long | int | String）
     */
    public static void login(Object id) {
        pbAzLogic.login(id);
    }

    /**
     * 会话登录，并指定所有登录参数 Model
     *
     * @param id         账号id，建议的类型：（long | int | String）
     * @param loginModel 此次登录的参数Model
     */
    public static void login(Object id, PbLoginModel loginModel) {
        pbAzLogic.login(id, loginModel);
    }


    /**
     * 获取当前会话账号id，如果未登录，则抛出异常
     *
     * @return 账号id
     */
    public static Object getLoginId() {
        return pbAzLogic.getLoginId();
    }

    /**
     * 获取当前会话账号id, 如果未登录，则返回默认值
     *
     * @param <T>          返回类型
     * @param defaultValue 默认值
     * @return 登录id
     */
    public static <T> T getLoginId(T defaultValue) {
        return pbAzLogic.getLoginId(defaultValue);
    }

    /**
     * 获取当前会话账号id, 如果未登录，则返回null
     *
     * @return 账号id
     */
    public static Object getLoginIdDefaultNull() {
        return pbAzLogic.getLoginIdDefaultNull();
    }

    /**
     * 获取当前会话账号id, 并转换为 String 类型
     *
     * @return 账号id
     */
    public static String getLoginIdAsString() {
        return pbAzLogic.getLoginIdAsString();
    }

    /**
     * 获取当前会话账号id, 并转换为 int 类型
     *
     * @return 账号id
     */
    public static int getLoginIdAsInt() {
        return pbAzLogic.getLoginIdAsInt();
    }

    /**
     * 获取当前会话账号id, 并转换为 long 类型
     *
     * @return 账号id
     */
    public static long getLoginIdAsLong() {
        return pbAzLogic.getLoginIdAsLong();
    }

    /**
     * 获取指定 token 对应的账号id，如果未登录，则返回 null
     *
     * @param tokenValue token
     * @return 账号id
     */
    public static Object getLoginIdByToken(String tokenValue) {
        return pbAzLogic.getLoginIdByToken(tokenValue);
    }

    /**
     * 获取当前 Token 的扩展信息（此函数只在jwt模式下生效）
     *
     * @param key 键值
     * @return 对应的扩展数据
     */
    public static Object getExtra(String key) {
        return pbAzLogic.getExtra(key);
    }

    /**
     * 获取当前会话的 token 参数信息
     *
     * @return token 参数信息
     */
    public static PbTokenInfo getTokenInfo() {
        return pbAzLogic.getTokenInfo();
    }


}
