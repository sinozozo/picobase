package com.picobase.console;

import com.picobase.annotation.PbEventReceiver;
import com.picobase.console.event.AdminAuthRequestEvent;
import com.picobase.console.event.AdminAuthWithPasswordEvent;
import com.picobase.console.event.TimePosition;
import org.springframework.stereotype.Service;

@Service
public class EventHandlerForAdminControllerTest2 {


    @PbEventReceiver
    public void onAdminAuthWithPasswordEvent(AdminAuthWithPasswordEvent event){
        if(event.timePosition== TimePosition.AFTER&&AdminControllerTest.afterError){
            AdminControllerTest.AdminAuthWithPasswordEvent_AFTER++;
            throw new RuntimeException("error");
        }
    }
}
