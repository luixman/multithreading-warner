package ru.multithreadingwarner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class MultithreadingWarnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultithreadingWarnerApplication.class, args);
	}

}
