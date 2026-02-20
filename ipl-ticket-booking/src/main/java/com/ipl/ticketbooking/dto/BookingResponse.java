package com.ipl.ticketbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {
    
    private Long bookingId;
    private String bookingReference;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal bookingFee;
    private Integer totalSeats;
    private LocalDateTime bookingDate;
    private List<SeatInfo> seats;
    
    // Constructors
    public BookingResponse() {}
    
    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getBookingFee() { return bookingFee; }
    public void setBookingFee(BigDecimal bookingFee) { this.bookingFee = bookingFee; }
    
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    
    public List<SeatInfo> getSeats() { return seats; }
    public void setSeats(List<SeatInfo> seats) { this.seats = seats; }
    
    // Inner class for seat information
    public static class SeatInfo {
        private Long seatId;
        private String seatNumber;
        private String rowNumber;
        private String category;
        private BigDecimal price;
        
        public SeatInfo() {}
        
        public SeatInfo(Long seatId, String seatNumber, String rowNumber, String category, BigDecimal price) {
            this.seatId = seatId;
            this.seatNumber = seatNumber;
            this.rowNumber = rowNumber;
            this.category = category;
            this.price = price;
        }
        
        // Getters and Setters
        public Long getSeatId() { return seatId; }
        public void setSeatId(Long seatId) { this.seatId = seatId; }
        
        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
        
        public String getRowNumber() { return rowNumber; }
        public void setRowNumber(String rowNumber) { this.rowNumber = rowNumber; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}