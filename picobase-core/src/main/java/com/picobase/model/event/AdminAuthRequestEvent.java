package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.AdminModel;

/**
 * admin 认证成功后的 Event
 */
public class AdminAuthRequestEvent implements PbEvent {
    public String token;
    public AdminModel admin;

    public AdminAuthRequestEvent(String token, AdminModel admin) {
        this.token = token;
        this.admin = admin;
    }
}
