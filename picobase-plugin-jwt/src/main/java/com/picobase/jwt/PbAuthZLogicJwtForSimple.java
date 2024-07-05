package com.picobase.jwt;

import com.picobase.PbUtil;
import com.picobase.jwt.error.PbJwtErrorCode;
import com.picobase.jwt.exception.PbJwtException;
import com.picobase.logic.authz.PbAuthZLogic;

import java.util.Map;

/**
 * 整合 jwt -- Simple 简单模式
 */
public class PbAuthZLogicJwtForSimple extends PbAuthZLogic {

    /**
     * 整合 jwt -- Simple模式
     */
    public PbAuthZLogicJwtForSimple() {
        super(PbUtil.TYPE);
    }

    /**
     * 整合 jwt -- Simple模式
     *
     * @param loginType 账号体系标识
     */
    public PbAuthZLogicJwtForSimple(String loginType) {
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

    // ------ 重写方法

    /**
     * 创建一个TokenValue
     */
    @Override
    public String createTokenValue(Object loginId, String device, long timeout, Map<String, Object> extraData) {
        return PbJwtUtil.createToken(loginType, loginId, extraData, jwtSecretKey());
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
        return PbJwtUtil.getPayloadsNotCheck(tokenValue, loginType, jwtSecretKey()).get(key);
    }


    @Override
    public boolean getConfigOfIsShare() {
        // 为确保 jwt-simple 模式的 token Extra 数据生成不受旧token影响，这里必须让 is-share 恒为 false
        // 即：在使用 jwt-simple 模式后，即使配置了 is-share=true 也不能复用旧 Token，必须每次创建新 Token
        return false;
    }

    /**
     * 重写返回：支持 extra 扩展参数
     */
    @Override
    public boolean isSupportExtra() {
        return true;
    }

}
