package com.picobase.jwt;


import com.picobase.PbUtil;
import com.picobase.cache.PbCache;
import com.picobase.context.PbHolder;
import com.picobase.exception.ApiDisabledException;
import com.picobase.exception.NotLoginException;
import com.picobase.exception.PbException;
import com.picobase.jwt.error.PbJwtErrorCode;
import com.picobase.jwt.exception.PbJwtException;
import com.picobase.logic.authz.PbAuthZLogic;
import com.picobase.logic.authz.PbTokenInfo;
import com.picobase.session.PbSession;
import com.picobase.util.CommonHelper;
import com.picobase.util.PbConstants;

import java.util.List;
import java.util.Map;

/**
 * 整合 jwt -- Mixin 混入模式
 */
public class PbAuthZLogicJwtForMixin extends PbAuthZLogic {

    /**
     * 整合 jwt -- Mixin 混入
     */
    public PbAuthZLogicJwtForMixin() {
        super(PbUtil.TYPE);
    }

    /**
     * 整合 jwt -- Mixin 混入
     *
     * @param loginType 账号体系标识
     */
    public PbAuthZLogicJwtForMixin(String loginType) {
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
     * 获取指定 token 的 Token-Session，如果该 PbSession 尚未创建，isCreate代表是否新建并返回
     *
     * @param tokenValue token值
     * @param isCreate   是否新建
     * @return session对象
     */
    public PbSession getTokenSessionByToken(String tokenValue, boolean isCreate) {
        if (CommonHelper.isEmpty(tokenValue)) {
            throw new PbException("Token-Session 获取失败：token 不能为空");
        }
        long timeout = getTokenTimeout(tokenValue);
        return getSessionBySessionId(splicingKeyTokenSession(tokenValue), isCreate, timeout, session -> {
            // 这里是该 Token-Session 首次创建时才会被执行的方法：
            // 		设定这个 PbSession 的各种基础信息：类型、账号体系、Token 值
            session.setType(PbConstants.SESSION_TYPE__TOKEN);
            session.setLoginType(getLoginType());
            session.setToken(tokenValue);
        });
    }

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
        // ...

        // 从当前 [storage存储器] 里删除
        PbHolder.getStorage().delete(splicingKeyJustCreatedSave());

        // 如果打开了Cookie模式，则把cookie清除掉
        if (getConfigOrGlobal().getIsReadCookie()) {
            PbHolder.getResponse().deleteCookie(getTokenName());
        }
    }

    /**
     * [禁用] 会话注销，根据账号id 和 设备类型
     */
    @Override
    public void logout(Object loginId, String device) {
        throw new ApiDisabledException();
    }

    /**
     * [禁用] 会话注销，根据指定 Token
     */
    @Override
    public void logoutByTokenValue(String tokenValue) {
        throw new ApiDisabledException();
    }

    /**
     * [禁用] 踢人下线，根据账号id 和 设备类型
     */
    @Override
    public void kickout(Object loginId, String device) {
        throw new ApiDisabledException();
    }

    /**
     * [禁用] 踢人下线，根据指定 Token
     */
    @Override
    public void kickoutByTokenValue(String tokenValue) {
        throw new ApiDisabledException();
    }

    /**
     * [禁用] 顶人下线，根据账号id 和 设备类型
     */
    @Override
    public void replaced(Object loginId, String device) {
        throw new ApiDisabledException();
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

    /**
     * 删除 Token-Id 映射
     */
    @Override
    public void deleteTokenToIdMapping(String tokenValue) {
        // not action
    }

    /**
     * 更改 Token 指向的 账号Id 值
     */
    @Override
    public void updateTokenToIdMapping(String tokenValue, Object loginId) {
        // not action
    }

    /**
     * 存储 Token-Id 映射
     */
    @Override
    public void saveTokenToIdMapping(String tokenValue, Object loginId, long timeout) {
        // not action
    }

    // ------------------- 过期时间相关 -------------------

    /**
     * 获取当前登录者的 token 剩余有效时间 (单位: 秒)
     */
    @Override
    public long getTokenTimeout() {
        return PbJwtUtil.getTimeout(getTokenValue(), loginType, jwtSecretKey());
    }


    // ------------------- 会话管理 -------------------

    /**
     * [禁用] 根据条件查询Token
     */
    @Override
    public List<String> searchTokenValue(String keyword, int start, int size, boolean sortType) {
        throw new ApiDisabledException();
    }


    // ------------------- Bean对象代理 -------------------

    /**
     * 返回全局配置对象的 isShare 属性
     *
     * @return /
     */
    @Override
    public boolean getConfigOfIsShare() {
        return false;
    }

    /**
     * 返回全局配置对象的 maxTryTimes 属性
     *
     * @return /
     */
    @Override
    public int getConfigOfMaxTryTimes() {
        return -1;
    }

    /**
     * 重写返回：支持 extra 扩展参数
     */
    @Override
    public boolean isSupportExtra() {
        return true;
    }

}
