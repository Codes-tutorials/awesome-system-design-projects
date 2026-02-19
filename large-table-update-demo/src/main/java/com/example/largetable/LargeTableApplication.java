package com.example.largetable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LargeTableApplication {
    public static void main(String[] args) {
        SpringApplication.run(LargeTableApplication.class, args);
    }
}
