package com.picobase.scheduler;

import cn.hutool.core.util.ReflectUtil;
import com.picobase.strategy.PbStrategy;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * EN: Trigger timestamp, as long as the current timestamp is greater than this trigger event stamp, it is considered triggerable
 * CN: 触发时间戳，只要当前时间戳大于这个触发事件戳都视为可以触发
 */
public class SchedulerDefinition {

    private String cronExpression;

    private IScheduler scheduler;

    private long triggerTimestamp;

    public static SchedulerDefinition valueOf(String cron, Object bean, Method method) throws NoSuchMethodException, IllegalAccessException, InstantiationException, CannotCompileException, NotFoundException, InvocationTargetException {
        var schedulerDef = new SchedulerDefinition();
        schedulerDef.cronExpression = cron;

        // bytecode enhancements to avoid reflection
        schedulerDef.scheduler = EnhanceUtils.createScheduler(ReflectScheduler.valueOf(bean, method));
        schedulerDef.triggerTimestamp = PbStrategy.instance.nextTimestampByCronExpressionFunction.apply(cron, PbSchedulerBus.currentTimestampWithZone());
        ReflectUtil.setAccessible(method);
        return schedulerDef;
    }

    public static SchedulerDefinition valueOf(String cron, Runnable runnable) {
        var schedulerDef = new SchedulerDefinition();
        schedulerDef.cronExpression = cron;
        schedulerDef.scheduler = RunnableScheduler.valueOf(runnable);
        schedulerDef.triggerTimestamp = PbStrategy.instance.nextTimestampByCronExpressionFunction.apply(cron, PbSchedulerBus.currentTimestampWithZone());
        return schedulerDef;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public SchedulerDefinition setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public IScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(IScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public long getTriggerTimestamp() {
        return triggerTimestamp;
    }

    public void setTriggerTimestamp(long triggerTimestamp) {
        this.triggerTimestamp = triggerTimestamp;
    }
}
