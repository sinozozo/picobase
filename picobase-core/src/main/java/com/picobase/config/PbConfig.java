package com.picobase.config;

import java.io.Serializable;

/**
 * PicoBase 配置类
 */
public class PbConfig implements Serializable {

    /**
     * Cookie配置对象
     */
    public PbCookieConfig cookie = new PbCookieConfig();
    /**
     * S3 配置
     */
    public S3Config s3 = new S3Config();
    /**
     * token 名称 （同时也是： cookie 名称、提交 token 时参数的名称、存储 token 时的 key 前缀）
     */
    private String tokenName = "Authorization";
    /**
     * token 有效期（单位：秒） 默认30天，-1 代表永久有效
     */
    private long timeout = 60 * 60 * 24 * 30;
    /**
     * token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
     * （例如可以设置为 1800 代表 30 分钟内无操作就冻结）
     */
    private long activeTimeout = -1;
    /**
     * 是否允许同一账号多地同时登录 （为 true 时允许一起登录, 为 false 时新登录挤掉旧登录）
     */
    private Boolean isConcurrent = true;
    /**
     * 是否启用动态 activeTimeout 功能，如不需要请设置为 false，节省缓存请求次数
     */
    private Boolean dynamicActiveTimeout = false;
    /**
     * 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个 token, 为 false 时每次登录新建一个 token）
     */
    private Boolean isShare = true;
    /**
     * 同一账号最大登录数量，-1代表不限 （只有在 isConcurrent=true, isShare=false 时此配置项才有意义）
     */
    private int maxLoginCount = 12;
    /**
     * 在每次创建 token 时的最高循环次数，用于保证 token 唯一性（-1=不循环尝试，直接使用）
     */
    private int maxTryTimes = 12;
    /**
     * 是否尝试从请求体里读取 token
     */
    private Boolean isReadBody = true;
    /**
     * 是否尝试从 header 里读取 token
     */
    private Boolean isReadHeader = true;
    /**
     * 是否尝试从 cookie 里读取 token
     */
    private Boolean isReadCookie = true;
    /**
     * 是否在登录后将 token 写入到响应头
     */
    private Boolean isWriteHeader = false;
    /**
     * token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
     */
    private String tokenStyle = "uuid";
    /**
     * 是否打开自动续签 activeTimeout （如果此值为 true, 框架会在每次直接或间接调用 getLoginId() 时进行一次过期检查与续签操作）
     */
    private Boolean autoRenew = true;
    /**
     * token 前缀, 前端提交 token 时应该填写的固定前缀，格式样例(satoken: Bearer xxxx-xxxx-xxxx-xxxx)
     */
    private String tokenPrefix;
    /**
     * 获取 Token-Session 时是否必须登录（如果配置为true，会在每次获取 getTokenSession() 时校验当前是否登录）
     */
    private Boolean tokenSessionCheckLogin = true;
    /**
     * 是否在初始化配置时在控制台打印版本字符画
     */
    private Boolean isPrint = true;
    /**
     * 是否打印操作日志
     */
    private Boolean isLog = false;
    /**
     * 是否打印彩色日志
     */
    private Boolean isColorLog = null;
    /**
     * 日志等级 int 值（1=trace、2=debug、3=info、4=warn、5=error、6=fatal），此值与 logLevel 联动
     */
    private int logLevelInt = 1;
    /**
     * 配置当前项目的网络访问地址
     */
    private String currDomain;
    /**
     * 默认 PbCache 实现类中，每次清理过期数据间隔的时间（单位: 秒），默认值30秒，设置为 -1 代表不启动定时清理
     */
    private int dataRefreshPeriod = 30;
    /**
     * Http Basic 认证的默认账号和密码
     */
    private String basic = "";

    /**
     * jwt秘钥（只有集成 jwt 相关模块时此参数才会生效）
     */
    private String jwtSecretKey;


    /**
     * @return 是否在初始化配置时在控制台打印版本字符画
     */
    public Boolean getIsPrint() {
        return isPrint;
    }

    /**
     * @param print 是否在初始化配置时在控制台打印版本字符画
     * @return 对象本身
     */
    public PbConfig setIsPrint(Boolean print) {
        isPrint = print;
        return this;
    }

    /**
     * 是否开启框架日志
     *
     * @return 是否开启
     */
    public Boolean getIsLog() {
        return isLog;
    }

    /**
     * @param log 是否开启框架日志
     * @return 对象自身
     */
    public PbConfig setIsLog(Boolean log) {
        isLog = log;
        return this;
    }

    /**
     * @return 是否打印彩色日志
     */
    public Boolean getIsColorLog() {
        return isColorLog;
    }

    /**
     * 是否打印彩色日志
     *
     * @param colorLog 是否打印彩色日志
     * @return 对象自身
     */
    public PbConfig setIsColorLog(Boolean colorLog) {
        isColorLog = colorLog;
        return this;
    }

    /**
     * @return 日志等级 int 值（1=trace、2=debug、3=info、4=warn、5=error、6=fatal），此值与 logLevel 联动
     */
    public int getLogLevelInt() {
        return logLevelInt;
    }

    /**
     * @param logLevelInt 日志等级 int 值（1=trace、2=debug、3=info、4=warn、5=error、6=fatal），此值与 logLevel 联动
     * @return 对象自身
     */
    public PbConfig setLogLevelInt(int logLevelInt) {
        this.logLevelInt = logLevelInt;
        return this;
    }

    /**
     * @return 配置当前项目的网络访问地址
     */
    public String getCurrDomain() {
        return currDomain;
    }

    /**
     * @param currDomain 配置当前项目的网络访问地址
     * @return 对象自身
     */
    public PbConfig setCurrDomain(String currDomain) {
        this.currDomain = currDomain;
        return this;
    }

    /**
     * @return 默认 PbCache 实现类中，每次清理过期数据间隔的时间（单位: 秒），默认值30秒，设置为 -1 代表不启动定时清理
     */
    public int getDataRefreshPeriod() {
        return dataRefreshPeriod;
    }

    /**
     * @param dataRefreshPeriod 默认 PbCache 实现类中，每次清理过期数据间隔的时间（单位: 秒），默认值30秒，设置为 -1 代表不启动定时清理
     * @return 对象自身
     */
    public PbConfig setDataRefreshPeriod(int dataRefreshPeriod) {
        this.dataRefreshPeriod = dataRefreshPeriod;
        return this;
    }

    /**
     * @return token 名称 （同时也是： cookie 名称、提交 token 时参数的名称、存储 token 时的 key 前缀）
     */
    public String getTokenName() {
        return tokenName;
    }

    /**
     * @param tokenName token 名称 （同时也是： cookie 名称、提交 token 时参数的名称、存储 token 时的 key 前缀）
     * @return 对象自身
     */
    public PbConfig setTokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    /**
     * @return token 有效期（单位：秒） 默认30天，-1 代表永久有效
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * @param timeout token 有效期（单位：秒） 默认30天，-1 代表永久有效
     * @return 对象自身
     */
    public PbConfig setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * @return token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
     * （例如可以设置为 1800 代表 30 分钟内无操作就冻结）
     */
    public long getActiveTimeout() {
        return activeTimeout;
    }

    /**
     * @param activeTimeout token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
     *                      （例如可以设置为 1800 代表 30 分钟内无操作就冻结）
     * @return 对象自身
     */
    public PbConfig setActiveTimeout(long activeTimeout) {
        this.activeTimeout = activeTimeout;
        return this;
    }

    /**
     * @return 是否启用动态 activeTimeout 功能，如不需要请设置为 false，节省缓存请求次数
     */
    public Boolean getDynamicActiveTimeout() {
        return dynamicActiveTimeout;
    }

    /**
     * @param dynamicActiveTimeout 是否启用动态 activeTimeout 功能，如不需要请设置为 false，节省缓存请求次数
     * @return 对象自身
     */
    public PbConfig setDynamicActiveTimeout(Boolean dynamicActiveTimeout) {
        this.dynamicActiveTimeout = dynamicActiveTimeout;
        return this;
    }

    /**
     * @return 是否允许同一账号多地同时登录 （为 true 时允许一起登录, 为 false 时新登录挤掉旧登录）
     */
    public Boolean getIsConcurrent() {
        return isConcurrent;
    }

    /**
     * @param isConcurrent 是否允许同一账号多地同时登录 （为 true 时允许一起登录, 为 false 时新登录挤掉旧登录）
     * @return 对象自身
     */
    public PbConfig setIsConcurrent(Boolean isConcurrent) {
        this.isConcurrent = isConcurrent;
        return this;
    }

    /**
     * @return 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个token, 为 false 时每次登录新建一个 token）
     */
    public Boolean getIsShare() {
        return isShare;
    }

    /**
     * @param isShare 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个token, 为 false 时每次登录新建一个 token）
     * @return 对象自身
     */
    public PbConfig setIsShare(Boolean isShare) {
        this.isShare = isShare;
        return this;
    }

    /**
     * @return 同一账号最大登录数量，-1代表不限 （只有在 isConcurrent=true, isShare=false 时此配置项才有意义）
     */
    public int getMaxLoginCount() {
        return maxLoginCount;
    }

    /**
     * @param maxLoginCount 同一账号最大登录数量，-1代表不限 （只有在 isConcurrent=true, isShare=false 时此配置项才有意义）
     * @return 对象自身
     */
    public PbConfig setMaxLoginCount(int maxLoginCount) {
        this.maxLoginCount = maxLoginCount;
        return this;
    }

    /**
     * @return 在每次创建 token 时的最高循环次数，用于保证 token 唯一性（-1=不循环尝试，直接使用）
     */
    public int getMaxTryTimes() {
        return maxTryTimes;
    }

    /**
     * @param maxTryTimes 在每次创建 token 时的最高循环次数，用于保证 token 唯一性（-1=不循环尝试，直接使用）
     * @return 对象自身
     */
    public PbConfig setMaxTryTimes(int maxTryTimes) {
        this.maxTryTimes = maxTryTimes;
        return this;
    }

    /**
     * @return 是否尝试从请求体里读取 token
     */
    public Boolean getIsReadBody() {
        return isReadBody;
    }

    /**
     * @param isReadBody 是否尝试从请求体里读取 token
     * @return 对象自身
     */
    public PbConfig setIsReadBody(Boolean isReadBody) {
        this.isReadBody = isReadBody;
        return this;
    }

    /**
     * @return 是否尝试从 header 里读取 token
     */
    public Boolean getIsReadHeader() {
        return isReadHeader;
    }

    /**
     * @param isReadHeader 是否尝试从 header 里读取 token
     * @return 对象自身
     */
    public PbConfig setIsReadHeader(Boolean isReadHeader) {
        this.isReadHeader = isReadHeader;
        return this;
    }

    /**
     * @return 是否尝试从 cookie 里读取 token
     */
    public Boolean getIsReadCookie() {
        return isReadCookie;
    }

    /**
     * @param isReadCookie 是否尝试从 cookie 里读取 token
     * @return 对象自身
     */
    public PbConfig setIsReadCookie(Boolean isReadCookie) {
        this.isReadCookie = isReadCookie;
        return this;
    }

    /**
     * @return 是否在登录后将 token 写入到响应头
     */
    public Boolean getIsWriteHeader() {
        return isWriteHeader;
    }

    /**
     * @param isWriteHeader 是否在登录后将 token 写入到响应头
     * @return 对象自身
     */
    public PbConfig setIsWriteHeader(Boolean isWriteHeader) {
        this.isWriteHeader = isWriteHeader;
        return this;
    }

    /**
     * @return token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
     */
    public String getTokenStyle() {
        return tokenStyle;
    }

    /**
     * @param tokenStyle token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
     * @return 对象自身
     */
    public PbConfig setTokenStyle(String tokenStyle) {
        this.tokenStyle = tokenStyle;
        return this;
    }

    /**
     * @return 是否打开自动续签 activeTimeout （如果此值为 true, 框架会在每次直接或间接调用 getLoginId() 时进行一次过期检查与续签操作）
     */
    public Boolean getAutoRenew() {
        return autoRenew;
    }

    /**
     * @param autoRenew 是否打开自动续签 activeTimeout （如果此值为 true, 框架会在每次直接或间接调用 getLoginId() 时进行一次过期检查与续签操作）
     * @return 对象自身
     */
    public PbConfig setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
        return this;
    }

    /**
     * @return token 前缀, 前端提交 token 时应该填写的固定前缀，格式样例(satoken: Bearer xxxx-xxxx-xxxx-xxxx)
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * @param tokenPrefix token 前缀, 前端提交 token 时应该填写的固定前缀，格式样例(satoken: Bearer xxxx-xxxx-xxxx-xxxx)
     * @return 对象自身
     */
    public PbConfig setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
        return this;
    }

    /**
     * @return 获取 Token-Session 时是否必须登录（如果配置为true，会在每次获取 getTokenSession() 时校验当前是否登录）
     */
    public Boolean getTokenSessionCheckLogin() {
        return tokenSessionCheckLogin;
    }

    /**
     * @param tokenSessionCheckLogin 获取 Token-Session 时是否必须登录（如果配置为true，会在每次获取 getTokenSession() 时校验当前是否登录）
     * @return 对象自身
     */
    public PbConfig setTokenSessionCheckLogin(Boolean tokenSessionCheckLogin) {
        this.tokenSessionCheckLogin = tokenSessionCheckLogin;
        return this;
    }

    /**
     * @return Cookie 全局配置对象
     */
    public PbCookieConfig getCookie() {
        return cookie;
    }

    /**
     * @param cookie Cookie 全局配置对象
     * @return 对象自身
     */
    public PbConfig setCookie(PbCookieConfig cookie) {
        this.cookie = cookie;
        return this;
    }

    /**
     * @return S3 配置
     */
    public S3Config getS3() {
        return s3;
    }

    /**
     * @param s3 S3 配置信息
     * @return 对象自身
     */
    public PbConfig setS3(S3Config s3) {
        this.s3 = s3;
        return this;
    }

    /**
     * @return Http Basic 认证的默认账号和密码
     */
    public String getBasic() {
        return basic;
    }

    /**
     * @param basic Http Basic 认证的默认账号和密码
     * @return 对象自身
     */
    public PbConfig setBasic(String basic) {
        this.basic = basic;
        return this;
    }

    /**
     * @return jwt秘钥（只有集成 jwt 相关模块时此参数才会生效）
     */
    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    /**
     * @param jwtSecretKey jwt秘钥（只有集成 jwt 相关模块时此参数才会生效）
     * @return 对象自身
     */
    public PbConfig setJwtSecretKey(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
        return this;
    }


    @Override
    public String toString() {
        return "PbConfig{" +
                "cookie=" + cookie +
                ", s3=" + s3 +
                ", tokenName='" + tokenName + '\'' +
                ", timeout=" + timeout +
                ", activeTimeout=" + activeTimeout +
                ", isConcurrent=" + isConcurrent +
                ", dynamicActiveTimeout=" + dynamicActiveTimeout +
                ", isShare=" + isShare +
                ", maxLoginCount=" + maxLoginCount +
                ", maxTryTimes=" + maxTryTimes +
                ", isReadBody=" + isReadBody +
                ", isReadHeader=" + isReadHeader +
                ", isReadCookie=" + isReadCookie +
                ", isWriteHeader=" + isWriteHeader +
                ", tokenStyle='" + tokenStyle + '\'' +
                ", autoRenew=" + autoRenew +
                ", tokenPrefix='" + tokenPrefix + '\'' +
                ", tokenSessionCheckLogin=" + tokenSessionCheckLogin +
                ", isPrint=" + isPrint +
                ", isLog=" + isLog +
                ", isColorLog=" + isColorLog +
                ", logLevelInt=" + logLevelInt +
                ", currDomain='" + currDomain + '\'' +
                ", dataRefreshPeriod=" + dataRefreshPeriod +
                ", basic='" + basic + '\'' +
                ", jwtSecretKey='" + jwtSecretKey + '\'' +
                '}';
    }
}
