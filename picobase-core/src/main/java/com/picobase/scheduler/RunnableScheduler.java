package com.picobase.scheduler;

public class RunnableScheduler implements IScheduler {

    private Runnable runnable;

    public static RunnableScheduler valueOf(Runnable runnable) {
        var scheduler = new RunnableScheduler();
        scheduler.runnable = runnable;
        return scheduler;
    }

    @Override
    public void invoke() {
        runnable.run();
    }
}
