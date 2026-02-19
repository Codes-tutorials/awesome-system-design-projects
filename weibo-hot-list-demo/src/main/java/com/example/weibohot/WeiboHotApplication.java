package com.example.weibohot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeiboHotApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeiboHotApplication.class, args);
    }
}
