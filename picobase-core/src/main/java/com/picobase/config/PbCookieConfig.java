package com.picobase.config;

/**
 * Cookie写入 相关配置
 */
public class PbCookieConfig {

	/*
		Cookie 功能为浏览器通用标准，建议大家自行搜索文章了解各个属性的功能含义，此处源码仅做简单解释。
	 */

    /**
     * 作用域
     * <p> 写入 Cookie 时显式指定的作用域, 常用于单点登录二级域名共享 Cookie 的场景。 </p>
     * <p> 一般情况下你不需要设置此值，因为浏览器默认会把 Cookie 写到当前域名下。 </p>
     */
    private String domain;

    /**
     * 路径 （一般只有当你在一个域名下部署多个项目时才会用到此值。）
     */
    private String path;

    /**
     * 是否只在 https 协议下有效
     */
    private Boolean secure = false;

    /**
     * 是否禁止 js 操作 Cookie
     */
    private Boolean httpOnly = false;

    /**
     * 第三方限制级别（Strict=完全禁止，Lax=部分允许，None=不限制）
     */
    private String sameSite;

    /**
     * 获取：Cookie 作用域
     * <p> 写入 Cookie 时显式指定的作用域, 常用于单点登录二级域名共享 Cookie 的场景。 </p>
     * <p> 一般情况下你不需要设置此值，因为浏览器默认会把 Cookie 写到当前域名下。 </p>
     *
     * @return /
     */
    public String getDomain() {
        return domain;
    }

    /**
     * 写入：Cookie 作用域
     * <p> 写入 Cookie 时显式指定的作用域, 常用于单点登录二级域名共享 Cookie 的场景。 </p>
     * <p> 一般情况下你不需要设置此值，因为浏览器默认会把 Cookie 写到当前域名下。 </p>
     *
     * @param domain /
     * @return 对象自身
     */
    public PbCookieConfig setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * @return 路径  （一般只有当你在一个域名下部署多个项目时才会用到此值。）
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path 路径  （一般只有当你在一个域名下部署多个项目时才会用到此值。）
     * @return 对象自身
     */
    public PbCookieConfig setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * @return 是否只在 https 协议下有效
     */
    public Boolean getSecure() {
        return secure;
    }

    /**
     * @param secure 是否只在 https 协议下有效
     * @return 对象自身
     */
    public PbCookieConfig setSecure(Boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * @return 是否禁止 js 操作 Cookie
     */
    public Boolean getHttpOnly() {
        return httpOnly;
    }

    /**
     * @param httpOnly 是否禁止 js 操作 Cookie
     * @return 对象自身
     */
    public PbCookieConfig setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    /**
     * @return 第三方限制级别（Strict=完全禁止，Lax=部分允许，None=不限制）
     */
    public String getSameSite() {
        return sameSite;
    }

    /**
     * @param sameSite 第三方限制级别（Strict=完全禁止，Lax=部分允许，None=不限制）
     * @return 对象自身
     */
    public PbCookieConfig setSameSite(String sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    // toString
    @Override
    public String toString() {
        return "PbCookieConfig [domain=" + domain + ", path=" + path + ", secure=" + secure + ", httpOnly=" + httpOnly
                + ", sameSite=" + sameSite + "]";
    }

}
