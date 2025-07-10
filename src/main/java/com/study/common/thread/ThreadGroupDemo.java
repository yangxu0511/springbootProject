package com.study.common.thread;

public class ThreadGroupDemo {

    public static void main(String[] args) {

        ThreadGroup group = new ThreadGroup("group1");
        group.setMaxPriority(6);

        Thread thread = new Thread(group, "thread1");
        thread.setPriority(9);

        System.out.println("线组优先级：" + group.getMaxPriority());
        System.out.println("线程优先级：" + thread.getPriority());

    }
}
