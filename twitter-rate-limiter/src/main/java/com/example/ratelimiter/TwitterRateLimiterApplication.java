package com.example.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TwitterRateLimiterApplication {
    public static void main(String[] args) {
        SpringApplication.run(TwitterRateLimiterApplication.class, args);
    }
}
