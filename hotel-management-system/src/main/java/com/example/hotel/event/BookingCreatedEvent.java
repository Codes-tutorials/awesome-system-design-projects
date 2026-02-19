package com.example.hotel.event;

import com.example.hotel.model.Reservation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingCreatedEvent extends ApplicationEvent {
    private final Reservation reservation;

    public BookingCreatedEvent(Object source, Reservation reservation) {
        super(source);
        this.reservation = reservation;
    }
}
