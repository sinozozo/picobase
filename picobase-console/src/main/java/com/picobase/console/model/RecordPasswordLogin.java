package com.picobase.console.model;


import com.picobase.validator.Errors;

import static com.picobase.validator.Validation.*;

public class RecordPasswordLogin {

    private String identity;
    private String password;

    public Errors validate() {
        return validateObject(this,
                field(RecordPasswordLogin::getIdentity, required, length(1, 255)),
                field(RecordPasswordLogin::getPassword, required, length(1, 255)));
    }

    public String getIdentity() {
        return identity;
    }

    public RecordPasswordLogin setIdentity(String identity) {
        this.identity = identity;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RecordPasswordLogin setPassword(String password) {
        this.password = password;
        return this;
    }
}
