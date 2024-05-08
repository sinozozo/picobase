package com.picobase.event;


public interface IEventReceiver {
    boolean isAsync();

    void invoke(PbEvent event);

     Object getBean();
}
