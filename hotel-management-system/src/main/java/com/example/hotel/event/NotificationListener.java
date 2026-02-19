package com.example.hotel.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationListener {

    @Async
    @EventListener
    public void handleBookingCreated(BookingCreatedEvent event) {
        // Simulate sending an email (e.g., waiting for external SMTP server)
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("📧 SENT CONFIRMATION EMAIL to {} for Reservation ID: {}", 
                event.getReservation().getGuest().getEmail(),
                event.getReservation().getId());
    }
}
