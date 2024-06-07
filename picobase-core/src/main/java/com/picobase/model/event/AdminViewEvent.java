package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.AdminModel;

public class AdminViewEvent implements PbEvent {

    public AdminModel admin;

    public AdminViewEvent(AdminModel admin) {
        this.admin = admin;
    }
}
