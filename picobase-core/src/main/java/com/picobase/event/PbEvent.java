package com.picobase.event;

import cn.hutool.core.util.RandomUtil;

/**
 *  事件， 用户可自定义事件
 */
public interface PbEvent {

    /**
     * 这个返回的是一个用于确定事件在EventBus中的哪个线程池的执行的一个参数，只有异步事件才会有作用
     * <p>
     * 比如cpu是四核，那么EventBus中的executors线程池的数量为8个，通过取余可以确定异步事件在哪个线程池中执行。
     * 如果参数是0，0 % 8 = 0，那么异步事件最终会在executors[0]表示的线程池中执行
     * 如果参数是1，1 % 8 = 1，那么异步事件最终会在executors[1]表示的线程池中执行
     * 如果参数是8，8 % 8 = 0，那么异步事件最终会在executors[0]表示的线程池中执行
     * 如果参数是9，9 % 8 = 1，那么异步事件最终会在executors[1]表示的线程池中执行
     * <p>
     * 通过返回的参数，可以轻松控制异步事件在哪个线程池中去执行。
     * 因为EventBus中的线程池都是单线程线程池，如果将一些异步事件放在同一个线程池中执行，可以不用加锁，提高程序运行的效率。
     * <p>
     * 默认返回一个随机数，这就导致如果不重写这个方法，那么异步事件有可能会在EventBus中的任何一条线程池中去执行。
     *
     * @return 线程池的执行的一个参数
     */
    default int executorHash() {
        return RandomUtil.randomInt();
    }

}
