package com.picobase.event;

import com.picobase.PbManager;
import com.picobase.log.PbLog;
import com.picobase.util.StrFormatter;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class EventThreadFactory implements ThreadFactory {
    private static final PbLog log = PbManager.getLog();
    private final int poolNumber;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group;


    public EventThreadFactory(int poolNumber) {
        this.group = Thread.currentThread().getThreadGroup();
        this.poolNumber = poolNumber;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        var threadName = StrFormatter.format("pb-eventhandler-p{}-t{}", poolNumber + 1, threadNumber.getAndIncrement());

        var thread = new Thread(group, runnable, threadName);
        thread.setDaemon(false);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) -> log.error(t.toString(), e));
        return thread;
    }
}