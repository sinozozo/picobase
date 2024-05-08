package com.picobase.fun.strategy;

import com.picobase.event.PbEvent;

import java.util.function.Consumer;

@FunctionalInterface
public interface PbNoEventReceiverFunction extends Consumer<PbEvent> {
}
