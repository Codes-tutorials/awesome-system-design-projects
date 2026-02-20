package com.rideshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Uber Clone - Ride Sharing Application
 * 
 * Features:
 * - Real-time ride matching and tracking
 * - Driver and rider management
 * - Payment processing
 * - Push notifications
 * - Location-based services
 * - Surge pricing
 * - Trip history and analytics
 */
@SpringBootApplication
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class UberCloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(UberCloneApplication.class, args);
    }
}