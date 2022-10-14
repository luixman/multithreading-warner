package ru.multithreadingwarner.service;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ThreadPoolExecutorTimeSpy extends ThreadPoolExecutor {
    private final ThreadsController controller;

    public ThreadPoolExecutorTimeSpy(int nThread, int warnTime, TimeUnit timeUnitForWarnTime) {
        super(nThread, nThread, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        controller = new ThreadPoolExecutorTimeSpy.ThreadsController(warnTime, timeUnitForWarnTime);
        controller.start();

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
        private final Long warnTime; //в секундах

        public ThreadsController(int warnTime, TimeUnit timeUnitForWarnTime) {
            this.warnTime = timeUnitForWarnTime.toSeconds(warnTime);


        }

        @Override
        public void run() {

            try {
                while (true) {
                    if (threads.isEmpty()) {
                        sleep(warnTime);
                    } else {
                        long timeWait = warnTime;
                        System.out.println(timeWait);

                        for (Map.Entry<Runnable, Node> t : threads.entrySet()) {
                            // long now = new Date().getTime();
                            long now = Instant.now().getEpochSecond();
                            if (now - t.getValue().startTime.getEpochSecond() < warnTime) {
                                if (now - t.getValue().startTime.getEpochSecond() < timeWait)
                                    //Если прошло меньше времени, чем в варн тайм, то считаем минимальное время для сна
                                    timeWait = warnTime - (now - t.getValue().startTime.getEpochSecond());
                            } else {
                                if (!t.getValue().isWarned) {
                                    t.getValue().isWarned = true;
                                    printWarn(t.getValue());

                                }
                            }
                        }
                        sleep(timeWait*1000);
                    }
                }
            } catch (InterruptedException e) {
                log.info("is interrupted");
            }
        }

        public void add(Runnable r, String threadName) {
            Node node = new Node(threadName, Instant.now());
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
            // long workTime = new Date().getTime() - node.startTime.getTime();
            long workTime = Instant.now().minusSeconds(node.startTime.getEpochSecond()).getEpochSecond();
            int seconds = (int) (workTime) % 60;
            int minutes = (int) ((workTime / 60) % 60);
            int hours = (int) ((workTime / (60 * 60)) % 24);

            log.warn(String.format("%s execution takes too long: %02d:%02d:%02d", node.threadName, hours, minutes, seconds));
        }

        private void PrintClosedThread(Node node) {
            long workTime = Instant.now().minusSeconds(node.startTime.getEpochSecond()).getEpochSecond();
            // long workTime = new Date().getTime() - node.startTime.getTime();
            int seconds = (int) (workTime) % 60;
            int minutes = (int) ((workTime / 60) % 60);
            int hours = (int) ((workTime / (60 * 60)) % 24);

            log.info(String.format("%s is ended: %02d:%02d:%02d", node.threadName, hours, minutes, seconds));

        }

        private static class Node {
            String threadName;
            Instant startTime;
            boolean isWarned;

            public Node(String threadName, Instant startTime) {
                this.threadName = threadName;
                this.startTime = startTime;
            }
        }

    }
}
