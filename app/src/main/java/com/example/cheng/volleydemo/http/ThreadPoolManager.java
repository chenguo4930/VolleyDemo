/*
 * ThreadPoolManager    2017-04-06
 * Copyright(c) 2017 Chengguo Co.Ltd. All right reserved.
 *
 */
package com.example.cheng.volleydemo.http;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理
 *
 * @author cheng
 * @version 1.0.0
 * @since 2017-04-06
 */
public class ThreadPoolManager {

    private static ThreadPoolManager instance = new ThreadPoolManager();

    //阻塞式队列
    private LinkedBlockingQueue<Future<?>> taskQuene = new LinkedBlockingQueue<>();

    private ThreadPoolExecutor threadPoolExecutor;

    public static ThreadPoolManager getInstance() {
        return instance;
    }

    private ThreadPoolManager() {
        threadPoolExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4), handler);
        threadPoolExecutor.execute(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                FutureTask futureTask = null;
                try {
                    /**
                     * 阻塞式函数
                     */
                    Log.e("tag", "等待队列" + taskQuene.size());
                    futureTask = (FutureTask) taskQuene.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null) {
                    threadPoolExecutor.execute(futureTask);
                }
            }
        }
    };

    public <T> void execte(FutureTask<T> futureTask) throws InterruptedException {
        taskQuene.put(futureTask);
    }

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                taskQuene.put(new FutureTask<Object>(r, null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public <T> boolean removeTask(FutureTask futureTask) {
        boolean result = false;
        /**
         * 阻塞式队列是否含有线程
         */
        if (taskQuene.contains(futureTask)) {
            taskQuene.remove(futureTask);
        } else {
            result = threadPoolExecutor.remove(futureTask);
        }
        return result;
    }

}