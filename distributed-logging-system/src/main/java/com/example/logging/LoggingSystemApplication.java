package com.example.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LoggingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoggingSystemApplication.class, args);
    }

}
