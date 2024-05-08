package com.picobase.console;

import com.picobase.PbManager;
import com.picobase.listener.PbEventCenter;
import com.picobase.log.PbLog;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.logic.authz.PbTokenInfo;

/**
 * 多类型用户下用于区分Admin 和 user ，这里主要提供Admin登录相关接口， 其他能力请使用 PbUtil
 *
 */
public class PbAdminUtil {

    private static final PbLog log = PbManager.getLog();
    private PbAdminUtil() {
    }

    /**
     * 多账号体系下的类型标识
     */
    public static final String TYPE = "pbAdmin";

    /**
     * 底层使用的 PbAuthZLogic 对象
     */
    public static PbAuthZLogic pbAzLogic = new PbAuthZLogic(TYPE);


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
     * 获取当前会话的 token 参数信息
     *
     * @return token 参数信息
     */
    public static PbTokenInfo getTokenInfo() {
        return pbAzLogic.getTokenInfo();
    }

}
