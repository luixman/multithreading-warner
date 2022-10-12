package ru.multithreadingwarner.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import ru.multithreadingwarner.service.Task;
import ru.multithreadingwarner.service.ThreadPoolExecutorTimeSpy;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@RestController
@EnableWebMvc
@Slf4j
public class TestController {


    @PostConstruct
    public void init() throws Exception{

        ExecutorService ex = new ThreadPoolExecutorTimeSpy(3, 5,TimeUnit.SECONDS);


        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Task c = new Task();

            tasks.add(c);
            ex.submit(c);
        }

        Thread.sleep(20000);
        ex.shutdown();


    }


}
