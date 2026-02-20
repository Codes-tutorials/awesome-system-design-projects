package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableKafka
@EnableCaching
@EnableRetry
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class OrderServiceApplication {

    public static void main(String[] args) {
        // Set system properties for optimal performance
        System.setProperty("spring.jpa.properties.hibernate.jdbc.batch_size", "50");
        System.setProperty("spring.jpa.properties.hibernate.order_inserts", "true");
        System.setProperty("spring.jpa.properties.hibernate.order_updates", "true");
        System.setProperty("spring.jpa.properties.hibernate.jdbc.batch_versioned_data", "true");
        
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}