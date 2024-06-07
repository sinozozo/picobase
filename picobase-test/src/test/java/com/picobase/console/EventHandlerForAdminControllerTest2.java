package com.picobase.console;

import com.picobase.annotation.PbEventReceiver;
import com.picobase.model.event.AdminAuthWithPasswordEvent;
import com.picobase.model.event.TimePosition;
import org.springframework.stereotype.Service;

@Service
public class EventHandlerForAdminControllerTest2 {


    @PbEventReceiver
    public void onAdminAuthWithPasswordEvent(AdminAuthWithPasswordEvent event) {
        if (event.timePosition == TimePosition.AFTER && AdminControllerTest.afterError) {
            AdminControllerTest.AdminAuthWithPasswordEvent_AFTER++;
            throw new RuntimeException("error");
        }
    }
}
