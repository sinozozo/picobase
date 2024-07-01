package com.picobase.scheduler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.picobase.PbManager;
import com.picobase.annotation.PbScheduler;
import com.picobase.log.PbLog;
import com.picobase.strategy.PbStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PbSchedulerBus {


    private static final PbLog logger = PbManager.getLog();

    private static final ZoneId DEFAULT_ZONE_ID = TimeZone.getDefault().toZoneId();

    /**
     * bus 当前运行状态
     */
    private static boolean stop = false;

    private static final List<SchedulerDefinition> schedulerDefList = new CopyOnWriteArrayList<>();
    private static final long MILLIS_PER_SECOND = 1 * 1000;
    /**
     * scheduler默认只有一个单线程的线程池
     */
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory(1));

    /**
     * executor创建的线程id号
     */
    private static long threadId = 0;

    /**
     * 上一次trigger触发时间
     */
    private static long lastTriggerTimestamp = 0L;


    /**
     * 在scheduler中，最小的triggerTimestamp
     */
    private static long minTriggerTimestamp = 0L;

    static {
        executor.scheduleAtFixedRate(() -> {
            try {
                triggerPerSecond();
            } catch (Exception e) {
                logger.error("scheduler triggers an error.", e);
            }
        }, 0, MILLIS_PER_SECOND, TimeUnit.MILLISECONDS);
    }

    public static class SchedulerThreadFactory implements ThreadFactory {

        private final int poolNumber;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        public SchedulerThreadFactory(int poolNumber) {
            this.group = Thread.currentThread().getThreadGroup();
            this.poolNumber = poolNumber;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            var threadName = StrUtil.format("pb-scheduler-p{}-t{}", poolNumber, threadNumber.getAndIncrement());
            var thread = new Thread(group, runnable, threadName);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setUncaughtExceptionHandler((t, e) -> logger.error(t.toString(), e));
            threadId = thread.getId();
            return thread;
        }

    }


    public static void refreshMinTriggerTimestamp() {
        var minTimestamp = Long.MAX_VALUE;
        for (var scheduler : schedulerDefList) {
            if (scheduler.getTriggerTimestamp() < minTimestamp) {
                minTimestamp = scheduler.getTriggerTimestamp();
            }
        }
        minTriggerTimestamp = minTimestamp;
    }

    /**
     * 每一秒执行一次，如果这个任务执行时间过长超过，比如10秒，执行完成后，不会再执行10次
     */
    private static void triggerPerSecond() {
        var currentTimeMillis = System.currentTimeMillis();

        if (CollUtil.isEmpty(schedulerDefList)) {
            return;
        }


        // 有人向前调整过机器时间，重新计算scheduler里的triggerTimestamp
        // var diff = timestamp - lastTriggerTimestamp;
        if (currentTimeMillis < lastTriggerTimestamp) {
            for (SchedulerDefinition schedulerDef : schedulerDefList) {
                var nextTriggerTimestamp = PbStrategy.instance.nextTimestampByCronExpressionFunction.apply(schedulerDef.getCronExpression(), timestampWithZone(currentTimeMillis));
                schedulerDef.setTriggerTimestamp(nextTriggerTimestamp);
            }
            refreshMinTriggerTimestamp();
        }

        // diff > 0, 没有人调整时间或者有人向后调整过机器时间，可以忽略，因为向后调整时间时间戳一定会大于triggerTimestamp，所以一定会触发
        lastTriggerTimestamp = currentTimeMillis;

        // 如果minSchedulerTriggerTimestamp大于timestamp，说明没有可执行的scheduler
        if (currentTimeMillis < minTriggerTimestamp) {
            return;
        }

        var minTimestamp = Long.MAX_VALUE;
        var timestampZonedDataTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), TimeZone.getDefault().toZoneId());
        for (var scheduler : schedulerDefList) {
            var triggerTimestamp = scheduler.getTriggerTimestamp();
            if (triggerTimestamp <= currentTimeMillis) {
                // 到达触发时间，则执行runnable方法
                try {
                    scheduler.getScheduler().invoke();
                } catch (Exception e) {
                    logger.error("scheduler invoke exception,{}", e.getMessage());
                } catch (Throwable t) {
                    logger.error("scheduler invoke error,{}", t.getMessage());
                }
                // 重新设置下一次的触发时间戳
                triggerTimestamp = PbStrategy.instance.nextTimestampByCronExpressionFunction.apply(scheduler.getCronExpression(), timestampZonedDataTime);
                scheduler.setTriggerTimestamp(triggerTimestamp);
            }
            if (triggerTimestamp < minTimestamp) {
                minTimestamp = scheduler.getTriggerTimestamp();
            }
        }
        minTriggerTimestamp = minTimestamp;
    }

    public static void registerScheduler(SchedulerDefinition scheduler) {
        schedulerDefList.add(scheduler);
        refreshMinTriggerTimestamp();
    }


    /**
     * 不断执行的周期循环任务
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long period, TimeUnit unit) {

        return executor.scheduleAtFixedRate(safeRunnable(runnable), 0, period, unit);
    }


    /**
     * 固定延迟执行的任务
     */
    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {

        return executor.schedule(safeRunnable(runnable), delay, unit);
    }

    /**
     * cron表达式执行的任务
     */
    public static void scheduleCron(Runnable runnable, String cron) {
        if (stop) {
            return;
        }

        registerScheduler(SchedulerDefinition.valueOf(cron, runnable));
    }

    public static Executor threadExecutor(long currentThreadId) {
        return threadId == currentThreadId ? executor : null;
    }

    public static ZonedDateTime currentTimestampWithZone() {
        return timestampWithZone(System.currentTimeMillis());
    }

    public static ZonedDateTime timestampWithZone(long time) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), DEFAULT_ZONE_ID);
    }

    private static Runnable safeRunnable(Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.error("unknown exception,{}", e.getMessage());
                } catch (Throwable t) {
                    logger.error("unknown error,{}", t.getMessage());
                }
            }
        };
    }

    public synchronized static void shutdown() {
        if (stop) {
            return;
        }

        stop = true;

        try {
            Field field = PbSchedulerBus.class.getDeclaredField("executor");
            ReflectUtil.setAccessible(field);
            var executor = (ScheduledExecutorService) ReflectUtil.getStaticFieldValue(field);
            shutdown(executor);
        } catch (Throwable e) {
            logger.error("Pb Scheduler thread pool failed shutdown. {}", e.getMessage());
            return;
        }

        logger.info("Pb Scheduler shutdown gracefully.");
    }

    public static void shutdown(ExecutorService executor) {
        try {
            if (!executor.isTerminated()) {

                executor.shutdown();

                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }

            }
        } catch (Exception e) {
            logger.error("[{}] is failed to shutdown! ", executor, e);
        }
    }

    public static void inject(Object bean) {
        var clazz = bean.getClass();
        var methods = ReflectUtil.getMethods(clazz, m -> m.isAnnotationPresent(PbScheduler.class));
        if (ArrayUtil.isEmpty(methods)) {
            return;
        }


        if (!(clazz.getSuperclass().equals(Object.class) || clazz.isRecord())) { // not pojo class
            logger.warn("The message registration class [{}] is not a POJO class, and the parent class will not be scanned", clazz);
        }

        try {
            for (var method : methods) {
                var schedulerMethod = method.getAnnotation(PbScheduler.class);

                var paramClazzs = method.getParameterTypes();
                if (paramClazzs.length >= 1) {
                    throw new IllegalArgumentException(StrUtil.format("[class:{}] [method:{}] can not have any parameters", bean.getClass(), method.getName()));
                }

                var methodName = method.getName();

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException(StrUtil.format("[class:{}] [method:{}] must use 'public' as modifier!", bean.getClass().getName(), methodName));
                }

                if (Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException(StrUtil.format("[class:{}] [method:{}] can not use 'static' as modifier!", bean.getClass().getName(), methodName));
                }

                if (!methodName.startsWith("cron")) {
                    throw new IllegalArgumentException(StrUtil.format("[class:{}] [method:{}] must start with 'cron' as method name!"
                            , bean.getClass().getName(), methodName));
                }

                var scheduler = SchedulerDefinition.valueOf(schedulerMethod.cron(), bean, method);
                registerScheduler(scheduler);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
