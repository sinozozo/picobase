package com.picobase.model;

import com.picobase.util.PbConstants;


public class AdminModel extends BaseModel {

    private String email;
    private int avatar;
    private String passwordHash;

    public String getEmail() {
        return email;
    }

    public AdminModel setEmail(String email) {
        this.email = email;
        return this;
    }


    public String getPasswordHash() {
        return passwordHash;
    }

    public AdminModel setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public int getAvatar() {
        return avatar;
    }

    public AdminModel setAvatar(int avatar) {
        this.avatar = avatar;
        return this;
    }

    @Override
    public String tableName() {
        return PbConstants.TableName.ADMIN;
    }
}
