package ru.multithreadingwarner.service;

import java.util.concurrent.Callable;

public class Task implements Runnable {
    @Override
    public void run() {

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Task complete");

    }
}
