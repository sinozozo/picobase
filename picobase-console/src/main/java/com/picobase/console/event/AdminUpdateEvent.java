package com.picobase.console.event;

import com.picobase.event.PbEvent;
import com.picobase.model.AdminModel;

public class AdminUpdateEvent implements PbEvent {
    public AdminModel admin;
    public TimePosition timePosition;
}
