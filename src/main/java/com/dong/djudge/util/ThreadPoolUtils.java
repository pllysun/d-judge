package com.dong.djudge.util;

import java.util.concurrent.*;

/**
 * @Author: Himit_ZH
 * @Date: 2021/12/21 12:06
 * @Description:
 */
public class ThreadPoolUtils {

    private static ExecutorService executorService;

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    private ThreadPoolUtils() {
        //手动创建线程池.
        executorService = new ThreadPoolExecutor(
                // 核心线程数
                cpuNum,
                // 最大线程数。最多几个线程并发。
                cpuNum,
                //当非核心线程无任务时，几秒后结束该线程
                3,
                // 结束线程时间单位
                TimeUnit.SECONDS,
                //阻塞队列，限制等候线程数
                new LinkedBlockingDeque<>(200 * cpuNum),
                Executors.defaultThreadFactory(),
                //队列满了，尝试去和最早的竞争，也不会抛出异常！
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private static class PluginConfigHolder {
        private final static ThreadPoolUtils INSTANCE = new ThreadPoolUtils();
    }

    public static ThreadPoolUtils getInstance() {
        return PluginConfigHolder.INSTANCE;
    }

    public ExecutorService getThreadPool() {
        return executorService;
    }

}