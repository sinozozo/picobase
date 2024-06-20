package com.picobase.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.picobase.PbManager;
import com.picobase.log.PbLog;
import com.picobase.strategy.PbStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class PbEventBus {

    private static final PbLog log = PbManager.getLog();

    /**
     * EN: The size of the thread pool. Event's thread pool is often used to do time-consuming operations, so set it a little bigger
     * CN: 线程池的大小. event的线程池经常用来做一些耗时的操作，所以要设置大一点
     */
    private final int EXECUTORS_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 4) * 2 + 1;

    private final ExecutorService[] executors = new ExecutorService[EXECUTORS_SIZE];
    /**
     * eventhandler mapping
     */
    private final Map<Class<? extends PbEvent>, List<IEventReceiver>> receiverMap = new HashMap<>();

    /**
     * eventbus 初始化线程池
     */
    public void init() {
        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new EventThreadFactory(i);
            var executor = Executors.newSingleThreadExecutor(namedThreadFactory);
            executors[i] = executor;
        }
    }

    /**
     * 优雅停机
     */
    public void destroy() {
        try {
            for (var executor : executors) {
                shutdown(executor);
            }
        } catch (Exception e) {
            log.error("Pb Event thread pool failed shutdown!", e);
        }
        log.info("Pb Event shutdown gracefully.");
    }

    private void shutdown(ExecutorService executor) {
        try {
            if (!executor.isTerminated()) {

                executor.shutdown();

                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }

            }
        } catch (Exception e) {
            log.error("[{}] is failed to shutdown! ", executor, e);
        }
    }

    /**
     * Publish the eventhandler
     *
     * @param event Event object
     */
    public void post(PbEvent event) {
        log.debug("[PbEventBus] post {}", event.getClass().getSimpleName());
        if (event == null) {
            return;
        }
        var clazz = event.getClass();
        var receivers = receiverMap.get(clazz);
        if (CollUtil.isEmpty(receivers)) {
            PbStrategy.instance.noEventReceiverFunction.accept(event);
            return;
        }
        for (var receiver : receivers) {
            if (receiver.isAsync()) {
                asyncExecute(event.executorHash(), () -> doReceiver(receiver, event));
            } else {
                doReceiver(receiver, event);
            }
        }
    }

    private void doReceiver(IEventReceiver receiver, PbEvent event) {
        log.debug("eventBus invoke, isAsync: {} eventhandler: {} receiver: {}", receiver.isAsync(), event.getClass().getSimpleName(), receiver.getClass().getSimpleName());
        try {
            receiver.invoke(event);
        } catch (Throwable t) {
            PbStrategy.instance.eventReceiverInvokeExceptionFunction.accept(receiver, event, t);
        }
    }

    public void asyncExecute(Runnable runnable) {
        asyncExecute(RandomUtil.randomInt(), runnable);
    }

    /**
     * Use the eventhandler thread specified by the hashcode to execute the task
     */
    public void asyncExecute(int executorHash, Runnable runnable) {
        executors[Math.abs(executorHash % EXECUTORS_SIZE)].execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                log.error("Pb eventBus unknown error", t);
            }
        });
    }

    /**
     * Register the eventhandler and its counterpart observer
     */
    public void registerEventReceiver(Class<? extends PbEvent> eventType, IEventReceiver receiver) {
        receiverMap.computeIfAbsent(eventType, it -> new ArrayList<>(1)).add(receiver);
        log.info("[PbEventBus] register eventhandler: ({}) receiver: ({})", eventType.getSimpleName(), receiver.getClass().getSimpleName());
    }

}


