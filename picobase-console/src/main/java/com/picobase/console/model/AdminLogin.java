package com.picobase.console.model;

import com.picobase.PbUtil;
import com.picobase.validator.Errors;
import com.picobase.validator.Is;

import static com.picobase.validator.Validation.*;

/**
 * 登录请求 DTO
 */
public class AdminLogin {
    private String identity;
    private String password;

    public String getIdentity() {
        return identity;
    }

    public AdminLogin setIdentity(String identity) {
        this.identity = identity;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AdminLogin setPassword(String password) {
        this.password = password;
        return this;
    }

    public Errors validate() {
        return PbUtil.validate(this,
                field("identity", this.identity, required, length(1, 255), Is.EmailFormat),
                field("password", this.password, required, length(6, 255)));
    }

    @Override
    public String toString() {
        return "AdminLogin{" +
                "identity='" + identity + '\'' +
                ", password='" + password + '\'' +
                '}';
    }


}
