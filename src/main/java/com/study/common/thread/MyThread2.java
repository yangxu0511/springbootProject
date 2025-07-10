package com.study.common.thread;

public class MyThread2 extends Thread {

    public static void main(String[] args) {
        MyThread2 t2 = new MyThread2();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            System.out.println(2222222);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
