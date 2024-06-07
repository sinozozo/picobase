package com.picobase.model.event;

import com.picobase.event.PbEvent;
import com.picobase.model.AdminModel;
import com.picobase.persistence.repository.Page;

public class AdminsListEvent implements PbEvent {
    public Page<AdminModel> result;

    public AdminsListEvent(Page<AdminModel> result) {
        this.result = result;
    }

}
