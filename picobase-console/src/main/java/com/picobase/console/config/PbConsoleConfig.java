package com.picobase.console.config;


import com.picobase.config.PbConfig;

/**
 * Admin 模块配置对象
 */
public class PbConsoleConfig {

    /**
     * 是否开启全局登录校验，如果为 false，则不再拦截请求出现登录页
     */
    private Boolean auth = true;

    /**
     * 用户名
     */
    private String identity;

    /**
     * 密码
     */
    private String password;


    /**
     * 配置拦截的路径，逗号分隔
     */
    private String include = "/**";

    /**
     * 配置拦截的路径，逗号分隔
     */
    private String exclude = "";

    /**
     *  是否打印执行sql
     */
    private boolean sqlLogEnable = false;

    public Boolean getAuth() {
        return auth;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public String getIdentity() {
        return identity;
    }

    public PbConsoleConfig setIdentity(String identity) {
        this.identity = identity;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public PbConsoleConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }


    /**
     * @return 是否打印 mapper 构造的 sql
     */
    public boolean isSqlLogEnable() {
        return sqlLogEnable;
    }

    /**
     * 是否打印 mapper 构造的 sql
     * @param sqlLog true 打印 , false 不打印
     * @return 对象自身
     */
    public PbConsoleConfig setSqlLogEnable(boolean sqlLog) {
        this.sqlLogEnable = sqlLog;
        return this;
    }

    @Override
    public String toString() {
        return "PbConsoleConfig{" +
                "auth=" + auth +
                ", identity='" + identity + '\'' +
                ", password='" + password + '\'' +
                ", include='" + include + '\'' +
                ", exclude='" + exclude + '\'' +
                ", sqlLogEnable=" + sqlLogEnable +
                '}';
    }
}
