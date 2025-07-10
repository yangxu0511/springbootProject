package com.study.common.utils;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger 原子类
 */
public class AtomicIntegerTest {


    private static final int THREADS_CONUT = 20;
    // public static int count = 0;
    //  public static volatile int count = 0;
    public static AtomicInteger count = new AtomicInteger(0);
    //原子类ABA问题解决方案 java包下两个类可以解决
    //AtomicStampedReference 和 AtomicMarkableReference
    //分配一个pair对象
    //java1.8之后的LongAdder比AtomicLong更好用 性能更高


    public static void increase() {
        // count++;
        count.getAndIncrement();
    }

    public static void main(String[] args) {
        Thread[] threads = new Thread[THREADS_CONUT];
        for (int i = 0; i < THREADS_CONUT; i++) {
            threads[i] = new Thread(() -> { //lambda表达式 1.8之后才能用 报错请升级JDK版本
                for (int i1 = 0; i1 < 1000; i1++) {
                    increase();
                }
            });
            threads[i].start();
        }
        try {
            Thread.sleep(900);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(count);
    }

}
