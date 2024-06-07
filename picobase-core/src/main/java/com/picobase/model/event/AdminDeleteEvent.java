package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.AdminModel;

public class AdminDeleteEvent implements PbEvent {

    public AdminModel admin;
    public TimePosition timePosition;

    public AdminDeleteEvent(AdminModel admin, TimePosition timePosition) {
        this.admin = admin;
        this.timePosition = timePosition;

    }
}
