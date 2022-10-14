package ru.multithreadingwarner.service;



public class Task implements Runnable {
    @Override
    public void run() {

        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Task complete");

    }
}
