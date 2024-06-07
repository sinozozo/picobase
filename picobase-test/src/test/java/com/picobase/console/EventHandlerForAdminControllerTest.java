package com.picobase.console;

import com.picobase.annotation.PbEventReceiver;
import com.picobase.model.event.AdminAuthRequestEvent;
import com.picobase.model.event.AdminAuthWithPasswordEvent;
import com.picobase.model.event.TimePosition;
import org.springframework.stereotype.Service;

@Service
public class EventHandlerForAdminControllerTest {

    @PbEventReceiver
    public void onAdminAuthRequestEvent(AdminAuthRequestEvent event) {
        AdminControllerTest.AdminAuthRequestEvent++;
    }

    @PbEventReceiver
    public void onAdminAuthWithPasswordEvent(AdminAuthWithPasswordEvent event) {
        if (event.timePosition == TimePosition.BEFORE) {
            AdminControllerTest.AdminAuthWithPasswordEvent_BEFORE++;
        } else {
            AdminControllerTest.AdminAuthWithPasswordEvent_AFTER++;
        }
    }

}
