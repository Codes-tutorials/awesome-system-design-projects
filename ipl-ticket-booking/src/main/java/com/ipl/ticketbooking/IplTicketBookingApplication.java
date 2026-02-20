package com.ipl.ticketbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync
@EnableTransactionManagement
public class IplTicketBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(IplTicketBookingApplication.class, args);
    }
}