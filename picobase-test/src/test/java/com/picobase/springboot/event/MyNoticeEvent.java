
package com.picobase.springboot.event;

import com.picobase.event.PbEvent;

public class MyNoticeEvent implements PbEvent {

    private String message;

    public static MyNoticeEvent valueOf(String message) {
        var event = new MyNoticeEvent();
        event.setMessage(message);
        return event;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
