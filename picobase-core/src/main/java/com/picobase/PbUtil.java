package com.picobase;

import com.picobase.logic.PbLogic;
import com.picobase.validator.Error;
import com.picobase.validator.FieldRules;
import com.picobase.validator.Validation;

/**
 * PB 工具类 （面相使用者的门面类）
 */
public final class PbUtil {

    private PbUtil() {
    }

    /**
     * 多账号体系下的类型标识
     */
    public static final String TYPE = "user";

    /**
     * 底层使用的 PbLogic 对象
     */
    public static PbLogic pbLogic = new PbLogic(TYPE);


    // 会话查询

    /**
     * 判断当前会话是否已经登录
     *
     * @return 已登录返回 true，未登录返回 false
     */
    public static boolean isLogin() {
        return pbLogic.isLogin();
    }

    /**
     * 判断指定账号是否已经登录
     *
     * @return 已登录返回 true，未登录返回 false
     */
    public static boolean isLogin(Object loginId) {
        return pbLogic.isLogin(loginId);
    }


    public static void login(String admin) {
    }


    // 对象校验 ================================================================================================

    /**
     * 对象结构的校验
     *
     * @param obj        待校验对象
     * @param fieldRules 校验规则
     * @return 校验结果
     */
    public static Error validate(Object obj, FieldRules... fieldRules) {
        return Validation.validateObject(obj, fieldRules);
    }


    // 对象校验 end================================================================================================
}
