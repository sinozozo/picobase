package com.picobase.strategy;


import com.picobase.PbManager;
import com.picobase.exception.PbException;
import com.picobase.exception.RequestPathInvalidException;
import com.picobase.fun.strategy.*;
import com.picobase.logic.PbLogic;
import com.picobase.session.PbSession;
import com.picobase.util.PbConstants;
import com.picobase.util.PbInnerUtil;

import java.util.UUID;

public final class PbStrategy {
    private PbStrategy() {
    }

    public static final PbStrategy instance = new PbStrategy();

    /**
     * 请求 path 不允许出现的字符
     */
    public static String[] INVALID_CHARACTER = {
            "//", "\\",
            "%2e", "%2E",    // .
            "%2f", "%2F",    // /
            "%5c", "%5C",    // \
            "%25"    // 空格
    };

    /**
     * 校验请求 path 的算法
     */
    public PbCheckRequestPathFunction checkRequestPath = (requestPath, extArg1, extArg2) -> {

        // 不允许为null
        if (requestPath == null) {
            throw new RequestPathInvalidException("非法请求：null", null);
        }
        // 不允许包含非法字符
        for (String item : INVALID_CHARACTER) {
            if (requestPath.contains(item)) {
                throw new RequestPathInvalidException("非法请求：" + requestPath, requestPath);
            }
        }
        // 不允许出现跨目录
        if (requestPath.contains("/.") || requestPath.contains("\\.")) {
            throw new RequestPathInvalidException("非法请求：" + requestPath, requestPath);
        }
    };

    /**
     * 当请求 path 校验不通过时处理方案的算法，自定义示例：
     * <pre>
     * 		PbStrategy.instance.requestPathInvalidHandle = (e, extArg1, extArg2) -> {
     * 			// 自定义处理逻辑 ...
     *      };
     * </pre>
     */
    public PbRequestPathInvalidHandleFunction requestPathInvalidHandle = null;

    /**
     * 创建 Session 的策略
     */
    public PbCreateSessionFunction createSession = (sessionId) -> new PbSession(sessionId);

    /**
     * 重写创建 Session 的策略
     *
     * @param createSession /
     * @return /
     */
    public PbStrategy setCreateSession(PbCreateSessionFunction createSession) {
        this.createSession = createSession;
        return this;
    }

    /**
     * 创建 PbLogic 的算法
     */
    public PbCreatePbLogicFunction createPbLogic = (loginType) -> new PbLogic(loginType);

    /**
     * 创建 StpLogic 的算法
     *
     * @param createStpLogic /
     * @return /
     */
    public PbStrategy setCreatePbLogic(PbCreatePbLogicFunction createStpLogic) {
        this.createPbLogic = createStpLogic;
        return this;
    }

    /**
     * 生成唯一式 token 的算法
     */
    public PbGenerateUniqueTokenFunction generateUniqueToken = (elementName, maxTryTimes, createTokenFunction, checkTokenFunction) -> {

        // 为方便叙述，以下代码注释均假设在处理生成 token 的场景，但实际上本方法也可能被用于生成 code、ticket 等

        // 循环生成
        for (int i = 1; ; i++) {
            // 生成 token
            String token = createTokenFunction.get();

            // 如果 maxTryTimes == -1，表示不做唯一性验证，直接返回
            if (maxTryTimes == -1) {
                return token;
            }

            // 如果 token 在DB库查询不到数据，说明是个可用的全新 token，直接返回
            if (checkTokenFunction.apply(token)) {
                return token;
            }

            // 如果已经循环了 maxTryTimes 次，仍然没有创建出可用的 token，那么抛出异常
            if (i >= maxTryTimes) {
                throw new PbException(elementName + " 生成失败，已尝试" + i + "次，生成算法过于简单或资源池已耗尽");
            }
        }
    };

    /**
     * 生成唯一式 token 的算法
     *
     * @param generateUniqueToken /
     * @return /
     */
    public PbStrategy setGenerateUniqueToken(PbGenerateUniqueTokenFunction generateUniqueToken) {
        this.generateUniqueToken = generateUniqueToken;
        return this;
    }

    /**
     * 创建 Token 的策略
     */
    public PbCreateTokenFunction createToken = (loginId, loginType) -> {
        // 根据配置的tokenStyle生成不同风格的token
        String tokenStyle = PbManager.getPbLogic(loginType).getConfigOrGlobal().getTokenStyle();

        switch (tokenStyle) {
            // uuid
            case PbConstants.TOKEN_STYLE_UUID:
                return UUID.randomUUID().toString();

            // 简单uuid (不带下划线)
            case PbConstants.TOKEN_STYLE_SIMPLE_UUID:
                return UUID.randomUUID().toString().replaceAll("-", "");

            // 32位随机字符串
            case PbConstants.TOKEN_STYLE_RANDOM_32:
                return PbInnerUtil.getRandomString(32);

            // 64位随机字符串
            case PbConstants.TOKEN_STYLE_RANDOM_64:
                return PbInnerUtil.getRandomString(64);

            // 128位随机字符串
            case PbConstants.TOKEN_STYLE_RANDOM_128:
                return PbInnerUtil.getRandomString(128);

            // tik风格 (2_14_16)
            case PbConstants.TOKEN_STYLE_TIK:
                return PbInnerUtil.getRandomString(2) + "_" + PbInnerUtil.getRandomString(14) + "_" + PbInnerUtil.getRandomString(16) + "__";

            // 默认，还是uuid
            default:
                PbManager.getLog().warn("配置的 tokenStyle 值无效：{}，仅允许以下取值: " +
                        "uuid、simple-uuid、random-32、random-64、random-128、tik", tokenStyle);
                return UUID.randomUUID().toString();
        }
    };

    /**
     * 重写创建 Token 的策略
     *
     * @param createToken /
     * @return /
     */
    public PbStrategy setCreateToken(PbCreateTokenFunction createToken) {
        this.createToken = createToken;
        return this;
    }

    /**
     * 判断：集合中是否包含指定元素（模糊匹配）
     */
    public PbHasElementFunction hasElement = (list, element) -> {

        // 空集合直接返回false
        if (list == null || list.size() == 0) {
            return false;
        }

        // 先尝试一下简单匹配，如果可以匹配成功则无需继续模糊匹配
        if (list.contains(element)) {
            return true;
        }

        // 开始模糊匹配
        for (String patt : list) {
            if (PbInnerUtil.vagueMatch(patt, element)) {
                return true;
            }
        }

        // 走出for循环说明没有一个元素可以匹配成功
        return false;
    };

    /**
     * 判断：集合中是否包含指定元素（模糊匹配）
     *
     * @param hasElement /
     * @return /
     */
    public PbStrategy setHasElement(PbHasElementFunction hasElement) {
        this.hasElement = hasElement;
        return this;
    }
}
