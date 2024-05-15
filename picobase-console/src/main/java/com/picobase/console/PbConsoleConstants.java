package com.picobase.console;

public class PbConsoleConstants {


    public static final String SESSION_KEY_ADMIN = "admin";
    public static final String SESSION_KEY_USER = "user";

    public static final String REQUEST_INFO_KEY = "requestInfo";

    public static final String ExpandQueryParam = "expand";


    /**
     * 当用户配置了无需登录或配置文件中配置了管理员账号密码 使用该值作为 admin id
     */
    public static final String InnerAdminId = "pbAdminId";

    /**
     * 多账号体系下的类型标识
     */
    public static final String LOGIN_TYPE_ADMIN = "pbAdmin";
}
