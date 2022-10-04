package ru.multithreadingwarner.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class MyThreadPoolExecutor extends ThreadPoolExecutor {
    private final ThreadsController controller;

    public MyThreadPoolExecutor(int nThread, int warnTime) {//если надо, то реализовать TimeUnit Для варнов
        super(nThread, nThread, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        controller = new MyThreadPoolExecutor.ThreadsController(warnTime);
        controller.start();
        //сюда можно добавить лог старта задачи
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        controller.add(r,t.getName());
    }

    protected void afterExecute(Runnable r,Throwable t){
        super.afterExecute(r,t);
        controller.remove(r);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        controller.interrupt();
    }



    private static class ThreadsController extends Thread {
        private final Map<Runnable,Node> threads = new ConcurrentHashMap();
        private final Long warnTime;

        public ThreadsController(int WARN_TIME) {

            this.warnTime = (long) WARN_TIME * 1000;
        }

        @Override
        public void run() {

            try {
                while (true) {
                    if (threads.isEmpty()) {
                        sleep(warnTime);
                    } else {
                        long timeWait = warnTime;

                        for (Map.Entry<Runnable, Node> t : threads.entrySet()) {
                            long now = new Date().getTime();
                            if (now - t.getValue().startTime.getTime() < warnTime) {
                                if (now - t.getValue().startTime.getTime() < timeWait)
                                    //Если прошло меньше времени, чем в варн тайм, то считаем минимальное время для сна
                                    timeWait = warnTime - (now - t.getValue().startTime.getTime());
                            } else {
                                if (!t.getValue().isWarned) {
                                    t.getValue().isWarned = true;
                                    log.warn(t.getValue().threadName + " running too long -> " + ((now - t.getValue().startTime.getTime()) / 1000) + " seconds");
                                }
                            }
                        }
                        sleep(timeWait);

                    }
                }
            } catch (InterruptedException e) {
                log.info("is interrupted");
            }

        }

        public void add(Runnable r, String threadName) {
            Node node = new Node(threadName, new Date());
            threads.put(r, node);


        }

        public void remove(Runnable r) {
            Node current = threads.get(r);
            if (current.isWarned) {
                long timeToEnd = (new Date().getTime() - current.startTime.getTime()) / 1000;
                log.info(current.threadName + " is ended -> " + timeToEnd + " seconds");
            }
            threads.remove(r);
        }

        private static class Node {
            String threadName;
            Date startTime;

            boolean isWarned;

            public Node(String threadName, Date startTime) {
                this.threadName = threadName;
                this.startTime = startTime;
            }
        }
    }

}
