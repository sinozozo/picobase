package com.picobase.fun.strategy;

import com.picobase.event.IEventReceiver;
import com.picobase.event.PbEvent;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface PbEventReceiverInvokeExceptionFunction  {
    void accept(IEventReceiver receiver, PbEvent event,Throwable e);
}
