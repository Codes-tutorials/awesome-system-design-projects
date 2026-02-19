package com.example.hotel.service;

import com.example.hotel.event.BookingCreatedEvent;
import com.example.hotel.model.Reservation;
import com.example.hotel.model.ReservationStatus;
import com.example.hotel.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processPaymentWebhook(Long reservationId, boolean success) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.PAYMENT_PENDING) {
            log.warn("Reservation {} is not in PENDING state", reservationId);
            return;
        }

        if (success) {
            reservation.setStatus(ReservationStatus.BOOKED);
            log.info("Payment SUCCESS for Reservation {}", reservationId);
            
            // Send confirmation email only after payment success
            eventPublisher.publishEvent(new BookingCreatedEvent(this, reservation));
        } else {
            reservation.setStatus(ReservationStatus.CANCELLED);
            log.warn("Payment FAILED for Reservation {}", reservationId);
        }
        
        reservationRepository.save(reservation);
    }

    public String generatePaymentUrl(Long reservationId) {
        return "https://dummy-payment-gateway.com/pay?id=" + reservationId;
    }
}
