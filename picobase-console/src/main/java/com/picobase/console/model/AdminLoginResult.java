package com.picobase.console.model;

import com.picobase.model.AdminModel;

public class AdminLoginResult {
    private String token;
    private AdminModel admin;

    public String getToken() {
        return token;
    }

    public AdminLoginResult setToken(String token) {
        this.token = token;
        return this;
    }

    public AdminModel getAdmin() {
        return admin;
    }

    public AdminLoginResult setAdmin(AdminModel admin) {
        this.admin = admin;
        return this;
    }
}
