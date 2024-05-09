package com.picobase.console.json.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.picobase.console.PbConsoleInject;
import com.picobase.console.WebMvcConfig;

/**
 *  jackson mixin ，用于在不修改原始类的情况下，为类添加或修改序列化和反序列化的规则。
 *
 *  PbConsole中会有两套jackson objectmapper 配置规则， 分别用于http响应json序列化 和数据库json序列化
 *
 */
public abstract class AdminModelMixIn {
    @JsonIgnore
    private String passwordHash;
}
