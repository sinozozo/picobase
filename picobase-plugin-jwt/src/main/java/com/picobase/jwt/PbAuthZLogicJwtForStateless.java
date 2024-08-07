package com.picobase.jwt;


import com.picobase.PbUtil;
import com.picobase.cache.PbCache;
import com.picobase.context.PbHolder;
import com.picobase.exception.ApiDisabledException;
import com.picobase.exception.NotLoginException;
import com.picobase.jwt.error.PbJwtErrorCode;
import com.picobase.jwt.exception.PbJwtException;
import com.picobase.listener.PbEventCenter;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.logic.authz.PbLoginModel;
import com.picobase.logic.authz.PbTokenInfo;
import com.picobase.util.CommonHelper;

import java.util.Map;

/**
 * Sa整合 jwt -- Stateless 无状态模式
 */
public class PbAuthZLogicJwtForStateless extends PbAuthZLogic {

    /**
     * 整合 jwt -- Stateless 无状态
     */
    public PbAuthZLogicJwtForStateless() {
        super(PbUtil.TYPE);
    }

    /**
     * 整合 jwt -- Stateless 无状态
     *
     * @param loginType 账号体系标识
     */
    public PbAuthZLogicJwtForStateless(String loginType) {
        super(loginType);
    }

    /**
     * 获取jwt秘钥
     *
     * @return /
     */
    public String jwtSecretKey() {
        String keyt = getConfigOrGlobal().getJwtSecretKey();
        PbJwtException.throwByNull(keyt, "请配置jwt秘钥", PbJwtErrorCode.CODE_30205);
        return keyt;
    }

    //
    // ------ 重写方法
    //

    // ------------------- 获取token 相关 -------------------

    /**
     * 创建一个TokenValue
     */
    @Override
    public String createTokenValue(Object loginId, String device, long timeout, Map<String, Object> extraData) {
        return PbJwtUtil.createToken(loginType, loginId, device, timeout, extraData, jwtSecretKey());
    }

    /**
     * 获取当前会话的Token信息
     *
     * @return token信息
     */
    @Override
    public PbTokenInfo getTokenInfo() {
        PbTokenInfo info = new PbTokenInfo();
        info.tokenName = getTokenName();
        info.tokenValue = getTokenValue();
        info.isLogin = isLogin();
        info.loginId = getLoginIdDefaultNull();
        info.loginType = getLoginType();
        info.tokenTimeout = getTokenTimeout();
        info.sessionTimeout = PbCache.NOT_VALUE_EXPIRE;
        info.tokenSessionTimeout = PbCache.NOT_VALUE_EXPIRE;
        info.tokenActiveTimeout = PbCache.NOT_VALUE_EXPIRE;
        info.loginDevice = getLoginDevice();
        return info;
    }

    // ------------------- 登录相关操作 -------------------

    /**
     * 创建指定账号id的登录会话
     *
     * @param id         登录id，建议的类型：（long | int | String）
     * @param loginModel 此次登录的参数Model
     * @return 返回会话令牌
     */
    @Override
    public String createLoginSession(Object id, PbLoginModel loginModel) {

        // 1、先检查一下，传入的参数是否有效
        checkLoginArgs(id, loginModel);

        // 2、初始化 loginModel ，给一些参数补上默认值
        loginModel.build(getConfigOrGlobal());

        // 3、生成一个token
        String tokenValue = createTokenValue(id, loginModel.getDeviceOrDefault(), loginModel.getTimeout(), loginModel.getExtraData());

        // 4、$$ 发布事件：账号xxx 登录成功
        PbEventCenter.doLogin(loginType, id, tokenValue, loginModel);

        // 5、返回
        return tokenValue;
    }

    /**
     * 获取指定Token对应的账号id (不做任何特殊处理)
     */
    @Override
    public String getLoginIdNotHandle(String tokenValue) {
        try {
            Object loginId = PbJwtUtil.getLoginId(tokenValue, loginType, jwtSecretKey());
            return String.valueOf(loginId);
        } catch (PbJwtException e) {
            // CODE == 30204 时，代表token已过期，此时返回-3，以便外层更精确的显示异常信息
            if (e.getCode() == PbJwtErrorCode.CODE_30204) {
                return NotLoginException.TOKEN_TIMEOUT;
            }
            return null;
        }
    }

    /**
     * 会话注销
     */
    @Override
    public void logout() {
        // 如果连token都没有，那么无需执行任何操作
        String tokenValue = getTokenValue();
        if (CommonHelper.isEmpty(tokenValue)) {
            return;
        }

        // 从当前 [storage存储器] 里删除
        PbHolder.getStorage().delete(splicingKeyJustCreatedSave());

        // 如果打开了Cookie模式，则把cookie清除掉
        if (getConfigOrGlobal().getIsReadCookie()) {
            PbHolder.getResponse().deleteCookie(getTokenName());
        }
    }

    /**
     * 获取当前 Token 的扩展信息
     */
    @Override
    public Object getExtra(String key) {
        return getExtra(getTokenValue(), key);
    }

    /**
     * 获取指定 Token 的扩展信息
     */
    @Override
    public Object getExtra(String tokenValue, String key) {
        return PbJwtUtil.getPayloads(tokenValue, loginType, jwtSecretKey()).get(key);
    }


    // ------------------- 过期时间相关 -------------------

    /**
     * 获取指定 token 剩余有效时间 (单位: 秒)
     */
    @Override
    public long getTokenTimeout(String tokenValue) {
        return PbJwtUtil.getTimeout(getTokenValue(), loginType, jwtSecretKey());
    }


    // ------------------- id 反查 token 相关操作 -------------------

    /**
     * 返回当前会话的登录设备类型
     *
     * @return 当前令牌的登录设备类型
     */
    @Override
    public String getLoginDevice() {
        // 如果没有token，直接返回 null
        String tokenValue = getTokenValue();
        if (tokenValue == null) {
            return null;
        }
        // 如果还未登录，直接返回 null
        if (!isLogin()) {
            return null;
        }
        // 获取
        return PbJwtUtil.getPayloadsNotCheck(tokenValue, loginType, jwtSecretKey()).getStr(PbJwtUtil.DEVICE);
    }


    // ------------------- Bean对象代理 -------------------

    /**
     * [禁用] 返回持久化对象
     */
    @Override
    public PbCache getPbCache() {
        throw new ApiDisabledException();
    }

    /**
     * 重写返回：支持 extra 扩展参数
     */
    @Override
    public boolean isSupportExtra() {
        return true;
    }

}
