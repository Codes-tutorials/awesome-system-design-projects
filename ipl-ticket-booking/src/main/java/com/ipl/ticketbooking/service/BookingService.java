package com.ipl.ticketbooking.service;

import com.ipl.ticketbooking.dto.BookingRequest;
import com.ipl.ticketbooking.dto.BookingResponse;
import com.ipl.ticketbooking.exception.BookingException;
import com.ipl.ticketbooking.exception.SeatNotAvailableException;
import com.ipl.ticketbooking.model.*;
import com.ipl.ticketbooking.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CORE BOOKING SERVICE: Handles high-concurrency ticket booking
 * Implements distributed locking, optimistic locking, and retry mechanisms
 * Designed to handle millions of concurrent booking requests
 */
@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final BigDecimal BOOKING_FEE_PERCENTAGE = new BigDecimal("0.05"); // 5%
    private static final int MAX_SEATS_PER_BOOKING = 10;
    private static final int SEAT_LOCK_DURATION_MINUTES = 10;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingSeatRepository bookingSeatRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private SeatLockingService seatLockingService;
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * MAIN BOOKING METHOD: Handles concurrent seat booking with distributed locking
     * This method is the heart of the high-concurrency booking system
     */
    @Transactional
    @Retryable(value = {OptimisticLockingFailureException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 100, multiplier = 2))
    public BookingResponse bookSeats(BookingRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. Rate limiting check
            if (!rateLimitingService.isAllowed(request.getUserId(), "booking")) {
                throw new BookingException("Rate limit exceeded. Please try again later.");
            }
            
            // 2. Validate request
            validateBookingRequest(request);
            
            // 3. Acquire distributed locks for seats
            if (!seatLockingService.lockSeats(request.getSeatIds(), request.getUserId())) {
                throw new SeatNotAvailableException("Selected seats are currently being booked by another user");
            }
            
            try {
                // 4. Verify seat availability with optimistic locking
                List<Seat> seats = verifySeatAvailability(request.getSeatIds(), request.getMatchId());
                
                // 5. Calculate total amount
                BigDecimal totalAmount = calculateTotalAmount(seats);
                BigDecimal bookingFee = totalAmount.multiply(BOOKING_FEE_PERCENTAGE);
                BigDecimal finalAmount = totalAmount.add(bookingFee);
                
                // 6. Create booking record
                Booking booking = createBooking(request, finalAmount, bookingFee, seats.size());
                
                // 7. Reserve seats (mark as booked)
                reserveSeats(seats, booking);
                
                // 8. Send booking event to Kafka for downstream processing
                publishBookingEvent(booking, "BOOKING_CREATED");
                
                long processingTime = System.currentTimeMillis() - startTime;
                logger.info("Booking completed successfully for user {} in {}ms. Booking ID: {}", 
                           request.getUserId(), processingTime, booking.getId());
                
                return createBookingResponse(booking, seats);
                
            } finally {
                // Always release locks
                seatLockingService.unlockSeats(request.getSeatIds(), request.getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Booking failed for user {} and seats {}: {}", 
                        request.getUserId(), request.getSeatIds(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Validates booking request parameters
     */
    private void validateBookingRequest(BookingRequest request) {
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new BookingException("No seats selected");
        }
        
        if (request.getSeatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw new BookingException("Maximum " + MAX_SEATS_PER_BOOKING + " seats allowed per booking");
        }
        
        // Check if match exists and booking is allowed
        Match match = matchRepository.findById(request.getMatchId())
            .orElseThrow(() -> new BookingException("Match not found"));
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(match.getTicketSaleStart())) {
            throw new BookingException("Ticket sale has not started yet");
        }
        
        if (now.isAfter(match.getTicketSaleEnd())) {
            throw new BookingException("Ticket sale has ended");
        }
        
        // Verify user exists
        if (!userRepository.existsById(request.getUserId())) {
            throw new BookingException("User not found");
        }
    }
    
    /**
     * Verifies seat availability using optimistic locking
     * This prevents race conditions at the database level
     */
    private List<Seat> verifySeatAvailability(List<Long> seatIds, Long matchId) {
        List<Seat> seats = seatRepository.findByIdInAndMatchIdForUpdate(seatIds, matchId);
        
        if (seats.size() != seatIds.size()) {
            throw new SeatNotAvailableException("Some seats not found or not available for this match");
        }
        
        // Check each seat's availability
        for (Seat seat : seats) {
            if (!seat.isAvailable()) {
                throw new SeatNotAvailableException("Seat " + seat.getSeatNumber() + " is not available");
            }
        }
        
        return seats;
    }
    
    /**
     * Calculates total booking amount
     */
    private BigDecimal calculateTotalAmount(List<Seat> seats) {
        return seats.stream()
                   .map(Seat::getPrice)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Creates booking record
     */
    private Booking createBooking(BookingRequest request, BigDecimal totalAmount, 
                                 BigDecimal bookingFee, int seatCount) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new BookingException("User not found"));
        
        Match match = matchRepository.findById(request.getMatchId())
            .orElseThrow(() -> new BookingException("Match not found"));
        
        String bookingReference = generateBookingReference();
        
        Booking booking = new Booking(user, match, bookingReference, totalAmount, bookingFee, seatCount);
        return bookingRepository.save(booking);
    }
    
    /**
     * Reserves seats by updating their status and creating booking-seat relationships
     */
    private void reserveSeats(List<Seat> seats, Booking booking) {
        List<BookingSeat> bookingSeats = new ArrayList<>();
        
        for (Seat seat : seats) {
            // Update seat status
            seat.setStatus(SeatStatus.BOOKED);
            seat.releaseLock(); // Clear any temporary locks
            seatRepository.save(seat);
            
            // Create booking-seat relationship
            BookingSeat bookingSeat = new BookingSeat(booking, seat, seat.getPrice());
            bookingSeats.add(bookingSeat);
        }
        
        bookingSeatRepository.saveAll(bookingSeats);
    }
    
    /**
     * Publishes booking events to Kafka for downstream processing
     */
    private void publishBookingEvent(Booking booking, String eventType) {
        try {
            BookingEvent event = new BookingEvent(
                booking.getId(),
                booking.getUser().getId(),
                booking.getMatch().getId(),
                booking.getBookingReference(),
                eventType,
                LocalDateTime.now()
            );
            
            kafkaTemplate.send("booking-events", event);
            logger.debug("Published booking event: {} for booking {}", eventType, booking.getId());
            
        } catch (Exception e) {
            logger.error("Failed to publish booking event for booking {}", booking.getId(), e);
            // Don't fail the booking for event publishing issues
        }
    }
    
    /**
     * Creates booking response DTO
     */
    private BookingResponse createBookingResponse(Booking booking, List<Seat> seats) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setStatus(booking.getStatus().toString());
        response.setTotalAmount(booking.getTotalAmount());
        response.setBookingFee(booking.getBookingFee());
        response.setTotalSeats(booking.getTotalSeats());
        response.setBookingDate(booking.getBookingDate());
        
        // Add seat details
        List<BookingResponse.SeatInfo> seatInfos = seats.stream()
            .map(seat -> new BookingResponse.SeatInfo(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getRowNumber(),
                seat.getSeatCategory().getCategoryName(),
                seat.getPrice()
            ))
            .toList();
        response.setSeats(seatInfos);
        
        return response;
    }
    
    /**
     * Generates unique booking reference
     */
    private String generateBookingReference() {
        return "IPL" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Cancels booking and releases seats
     */
    @Transactional
    public void cancelBooking(Long bookingId, Long userId, String reason) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
            .orElseThrow(() -> new BookingException("Booking not found"));
        
        if (!booking.canBeCancelled()) {
            throw new BookingException("Booking cannot be cancelled");
        }
        
        // Release seats
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        for (BookingSeat bookingSeat : bookingSeats) {
            Seat seat = bookingSeat.getSeat();
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        }
        
        // Update booking status
        booking.cancel(reason);
        bookingRepository.save(booking);
        
        // Publish cancellation event
        publishBookingEvent(booking, "BOOKING_CANCELLED");
        
        logger.info("Booking {} cancelled by user {}", bookingId, userId);
    }
    
    // Inner class for Kafka events
    public static class BookingEvent {
        private Long bookingId;
        private Long userId;
        private Long matchId;
        private String bookingReference;
        private String eventType;
        private LocalDateTime timestamp;
        
        public BookingEvent(Long bookingId, Long userId, Long matchId, String bookingReference, 
                           String eventType, LocalDateTime timestamp) {
            this.bookingId = bookingId;
            this.userId = userId;
            this.matchId = matchId;
            this.bookingReference = bookingReference;
            this.eventType = eventType;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getMatchId() { return matchId; }
        public void setMatchId(Long matchId) { this.matchId = matchId; }
        
        public String getBookingReference() { return bookingReference; }
        public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}