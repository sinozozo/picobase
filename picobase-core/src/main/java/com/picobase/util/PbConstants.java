package com.picobase.util;

import java.util.regex.Pattern;

import static com.picobase.util.PbConstants.FieldName.*;

public final class PbConstants {


    /**
     * 存储在Storage中的 Collection key 名称
     */
    public static final String STORAGE_KEY_COLLECTION = "STORAGE_KEY_COLLECTION";
    /**
     * 数据库中 id 的默认长度
     */
    public static final int DEFAULT_ID_LENGTH = 15;

    public static final String ID_REGEX = "^[^\\@\\#\\$\\&\\|\\.\\,\\'\\\"\\\\\\/\\s]+$";

    public static final Pattern ID_REGEX_P = Pattern.compile(ID_REGEX);
    public static final Pattern COLLECTION_NAME_P = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    /**
     * 切面、拦截器、过滤器等各种组件的注册优先级顺序
     */
    public static final int ASSEMBLY_ORDER = -100;

    /**
     * 请求 path 校验过滤器的注册顺序
     */
    public static final int PATH_CHECK_FILTER_ORDER = -1000;


    // ------------------ 常量 key 标记

    /**
     * 常量 key 标记: 如果 token 为本次请求新创建的，则以此字符串为 key 存储在当前请求 str 中
     */
    public static final String JUST_CREATED = "JUST_CREATED_";

    /**
     * 常量 key 标记: 如果 token 为本次请求新创建的，则以此字符串为 key 存储在当前 request 中（不拼接前缀，纯Token）
     */
    public static final String JUST_CREATED_NOT_PREFIX = "JUST_CREATED_NOT_PREFIX_";

    /**
     * 常量 key 标记: 如果本次请求已经验证过 activeTimeout, 则以此 key 在 storage 中做一个标记
     */
    public static final String TOKEN_ACTIVE_TIMEOUT_CHECKED_KEY = "TOKEN_ACTIVE_TIMEOUT_CHECKED_KEY_";

    /**
     * 常量 key 标记: 在登录时，默认使用的设备类型
     */
    public static final String DEFAULT_LOGIN_DEVICE = "default-device";

    /**
     * 常量 key 标记: 在封禁账号时，默认封禁的服务类型
     */
    public static final String DEFAULT_DISABLE_SERVICE = "login";

    /**
     * 常量 key 标记: 在封禁账号时，默认封禁的等级
     */
    public static final int DEFAULT_DISABLE_LEVEL = 1;

    /**
     * 常量 key 标记: 在封禁账号时，可使用的最小封禁级别
     */
    public static final int MIN_DISABLE_LEVEL = 1;

    /**
     * 常量 key 标记: 账号封禁级别，表示未被封禁
     */
    public static final int NOT_DISABLE_LEVEL = -2;

    /**
     * 常量 key 标记: 在进行临时身份切换时使用的 key
     */
    public static final String SWITCH_TO_SAVE_KEY = "SWITCH_TO_SAVE_KEY_";

    /**
     * 常量 key 标记: 在进行 Token 二级验证时，使用的 key
     */
    @Deprecated
    public static final String SAFE_AUTH_SAVE_KEY = "SAFE_AUTH_SAVE_KEY_";

    /**
     * 常量 key 标记: 在进行 Token 二级认证时，写入的 value 值
     */
    public static final String SAFE_AUTH_SAVE_VALUE = "SAFE_AUTH_SAVE_VALUE";

    /**
     * 常量 key 标记: 在进行 Token 二级验证时，默认的业务类型
     */
    public static final String DEFAULT_SAFE_AUTH_SERVICE = "important";

    /**
     * 常量 key 标记: 临时 Token 认证模块，默认的业务类型
     */
    public static final String DEFAULT_TEMP_TOKEN_SERVICE = "record";


    // ------------------ token-style 相关

    /**
     * Token风格: uuid
     */
    public static final String TOKEN_STYLE_UUID = "uuid";

    /**
     * Token风格: 简单uuid (不带下划线)
     */
    public static final String TOKEN_STYLE_SIMPLE_UUID = "simple-uuid";

    /**
     * Token风格: 32位随机字符串
     */
    public static final String TOKEN_STYLE_RANDOM_32 = "random-32";

    /**
     * Token风格: 64位随机字符串
     */
    public static final String TOKEN_STYLE_RANDOM_64 = "random-64";

    /**
     * Token风格: 128位随机字符串
     */
    public static final String TOKEN_STYLE_RANDOM_128 = "random-128";

    /**
     * Token风格: tik风格 (2_14_16)
     */
    public static final String TOKEN_STYLE_TIK = "tik";


    // ------------------ PbSession 的类型

    /**
     * PbSession 的类型: Account-Session
     */
    public static final String SESSION_TYPE__ACCOUNT = "Account-Session";

    /**
     * PbSession 的类型: Token-Session
     */
    public static final String SESSION_TYPE__TOKEN = "Token-Session";

    /**
     * PbSession 的类型: Custom-Session
     */
    public static final String SESSION_TYPE__CUSTOM = "Custom-Session";


    // ------------------ 其它

    /**
     * 连接 Token 前缀和 Token 值的字符
     */
    public static final String TOKEN_CONNECTOR_CHAT = " ";


    /**
     * Content-Type  key
     */
    public static final String CONTENT_TYPE_KEY = "Content-Type";

    /**
     * Content-Type  text/plain; charset=utf-8
     */
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain; charset=utf-8";

    /**
     * Content-Type  application/json;charset=UTF-8
     */
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json;charset=UTF-8";

    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static final String APPLICATION_JSON_VALUE = "application/json";
    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

    // filter modifiersonst
    public static final String EachModifier = "each";
    public static final String IssetModifier = "isset";
    public static final String LengthModifier = "length";

    public static final Pattern ThumbSizeRegex = Pattern.compile("^(\\d+)x(\\d+)(t|b|f)?$");
    public static final String FIELD_VALUE_MODIFIER_ADD = "+";
    public static final String FIELD_VALUE_MODIFIER_SUBTRACT = "-";
    public static final String DefaultDateLayout = "yyyy-MM-dd HH:mm:ss";
    public static final String[] plainRequestAuthFields = new String[]{
            "@request.auth." + Id,
            "@request.auth." + CollectionId,
            "@request.auth." + CollectionName,
            "@request.auth." + Username,
            "@request.auth." + Email,
            "@request.auth." + EmailVisibility,
            "@request.auth." + Verified,
            "@request.auth." + Created,
            "@request.auth." + Updated
    };
    public static final String[] systemFieldNames = new String[]{
            CollectionId,
            CollectionName,
            Expand
    };
    public static final String[] authFieldNames = new String[]{
            Username,
            Email,
            EmailVisibility,
            Verified,
            TokenKey,
            PasswordHash,
            LastResetSentAt,
            LastVerificationSentAt
    };
    public static final Pattern IndirectExpandRegexPattern = Pattern.compile("^(\\w+)_via_(\\w+)$");
    public static final int DEFAULT_PER_PAGE = 30;
    public static final int MAX_PER_PAGE = 500;
    /**
     * form表单中可以包含的特别json payload
     */
    public static final String MultipartJsonKey = "@jsonPayload";
    public static final String REQUEST_INFO_KEY = "requestInfo";
    /**
     * 当用户配置了无需登录或配置文件中配置了管理员账号密码 使用该值作为 admin id
     */
    public static final String InnerAdminId = "pbAdminId";
    /**
     * 多账号体系下的类型标识
     */
    public static final String LOGIN_TYPE_ADMIN = "pbAdmin";
    public static String[] baseModelFieldNames = new String[]{
            Id,
            Created,
            Updated,
    };

    public static String[] FieldValueModifiers() {
        return new String[]{FIELD_VALUE_MODIFIER_ADD, FIELD_VALUE_MODIFIER_SUBTRACT};
    }

    public static String[] ArraybleFieldTypes() {
        return new String[]{FieldType.Select, FieldType.File, FieldType.Relation};
    }

    public static class TableName {
        public static final String ADMIN = "pb_admin";
        public static final String COLLECTION = "pb_collection";
        public static final String EXTERNAL_AUTHS = "pb_external_auths";


    }

    public static class CollectionType {
        public static final String Base = "base";
        public static final String Auth = "auth";
        public static final String View = "view";
    }

    public static class FieldType {
        // All valid field types;
        public static final String Text = "text";
        public static final String Number = "number";
        public static final String Bool = "bool";
        public static final String Email = "email";
        public static final String Url = "url";
        public static final String Editor = "editor";
        public static final String Date = "date";
        public static final String Select = "select";
        public static final String Json = "json";
        public static final String File = "file";
        public static final String Relation = "relation";
    }

    public static class FieldName {
        // commonly used field names
        public static final String Id = "id";
        public static final String Created = "created";
        public static final String Updated = "updated";
        public static final String CollectionId = "collectionId";
        public static final String CollectionName = "collectionName";
        public static final String Expand = "expand";
        public static final String Username = "username";
        public static final String Password = "password";
        public static final String OldPassword = "oldPassword";
        public static final String PasswordConfirm = "passwordConfirm";
        public static final String Email = "email";
        public static final String EmailVisibility = "emailVisibility";
        public static final String Verified = "verified";
        public static final String TokenKey = "tokenKey";
        public static final String PasswordHash = "passwordHash";
        public static final String LastResetSentAt = "lastResetSentAt";
        public static final String LastVerificationSentAt = "lastVerificationSentAt";
    }

    public static class QueryParam {
        public static final String SKIP_TOTAL = "skipTotal";
        public static final String SKIP_DATA = "skipData";
        public static final String PAGE = "page";
        public static final String PER_PAGE = "perPage";
        public static final String SORT = "sort";
        public static final String FILTER = "filter";
        public static final String FIELDS = "fields";
        public static final String EXPAND = "expand";
    }

    /**
     * jwt 中扩展字段 collectionId
     */
    public static final String JwtExtraFieldCollectionId = "collectionId";
}
