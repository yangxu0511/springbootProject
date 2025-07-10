package com.study.common.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.LockSupport;

public class MyThread implements Callable<Object> {

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newCachedThreadPool(); //Callable接口多线程可以有返回值
        MyThread myThread = new MyThread();
//		Future<Object> futureTask = executorService.submit(myThread);
        FutureTask<Object> futureTask = new FutureTask<Object>(myThread);
        executorService.submit(futureTask);
//		futureTask.cancel(true);

        try {
            System.out.println(futureTask.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object call() throws Exception {
        Thread.sleep(1000);
        return 2;
    }

}
