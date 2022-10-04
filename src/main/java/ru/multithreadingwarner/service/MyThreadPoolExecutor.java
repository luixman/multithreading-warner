package ru.multithreadingwarner.service;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class MyThreadPoolExecutor extends ThreadPoolExecutor {
    private final ThreadsController controller;

    public MyThreadPoolExecutor(int nThread, int warnTime, TimeUnit timeUnitForWarnTime) {//если надо, то реализовать TimeUnit Для варнов
        super(nThread, nThread, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        controller = new MyThreadPoolExecutor.ThreadsController(warnTime, timeUnitForWarnTime);
        controller.start();
        //сюда можно добавить лог старта задачи
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        controller.add(r, t.getName());
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        controller.remove(r);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        controller.interrupt();
    }


    private static class ThreadsController extends Thread {
        private final Map<Runnable, Node> threads = new ConcurrentHashMap();
        private final Long warnTime;

        public ThreadsController(int warnTime, TimeUnit timeUnitForWarnTime) {
            this.warnTime = timeUnitForWarnTime.toMillis(warnTime);


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
                                    printWarn(t.getValue());

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
                PrintClosedThread(current);
            }
            threads.remove(r);
        }

        private void printWarn(Node node) {
            long workTime = new Date().getTime() - node.startTime.getTime();
            int seconds = (int) (workTime / 1000) % 60;
            int minutes = (int) ((workTime / (1000 * 60)) % 60);
            int hours = (int) ((workTime / (1000 * 60 * 60)) % 24);

            log.warn(String.format("%s execution takes too long: %02d:%02d:%02d", node.threadName, hours, minutes, seconds));
        }

        private void PrintClosedThread(Node node){
            long workTime = new Date().getTime() - node.startTime.getTime();
            int seconds = (int) (workTime / 1000) % 60;
            int minutes = (int) ((workTime / (1000 * 60)) % 60);
            int hours = (int) ((workTime / (1000 * 60 * 60)) % 24);

            log.info(String.format("%s is ended: %02d:%02d:%02d",node.threadName, hours, minutes, seconds));

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
