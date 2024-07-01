package com.picobase.console.config;


import java.nio.file.Paths;

import static com.picobase.console.PbConsoleConstants.defaultDataDir;
import static com.picobase.console.PbConsoleConstants.localStorageDirName;

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
     * 是否打印执行sql
     */
    private boolean isDev = false;


    private S3Config s3Config;
    private String dataDirPath = Paths.get(System.getProperty("user.dir"), defaultDataDir, localStorageDirName).toString();

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
    public boolean isDev() {
        return isDev;
    }

    /**
     * 是否打印 mapper 构造的 sql
     *
     * @param sqlLog true 打印 , false 不打印
     * @return 对象自身
     */
    public PbConsoleConfig setIsDev(boolean sqlLog) {
        this.isDev = sqlLog;
        return this;
    }


    public S3Config getS3Config() {
        return s3Config;
    }

    public PbConsoleConfig setS3Config(S3Config s3Config) {
        this.s3Config = s3Config;
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
                ", isDev=" + isDev +
                ", s3Config=" + s3Config +
                ", dataDirPath='" + dataDirPath + '\'' +
                '}';
    }

    public String getDataDirPath() {
        return this.dataDirPath;
    }

    public PbConsoleConfig setDataDirPath(String dataDirPath) {
        this.dataDirPath = dataDirPath;
        return this;
    }
}
