package com.uber.analytics.events;

import com.uber.analytics.flink.sources.DriverEventSource.Location;
import java.io.Serializable;

/**
 * Driver event model class
 */
public class DriverEvent implements Serializable {
    
    private String driverId;
    private String eventType;
    private long timestamp;
    private Location location;
    private String status;
    private String vehicleType;
    private double rating;
    private String city;
    
    // Default constructor for serialization
    public DriverEvent() {}
    
    private DriverEvent(Builder builder) {
        this.driverId = builder.driverId;
        this.eventType = builder.eventType;
        this.timestamp = builder.timestamp;
        this.location = builder.location;
        this.status = builder.status;
        this.vehicleType = builder.vehicleType;
        this.rating = builder.rating;
        this.city = builder.city;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getDriverId() { return driverId; }
    public String getEventType() { return eventType; }
    public long getTimestamp() { return timestamp; }
    public Location getLocation() { return location; }
    public String getStatus() { return status; }
    public String getVehicleType() { return vehicleType; }
    public double getRating() { return rating; }
    public String getCity() { return city; }
    
    // Setters for serialization
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setLocation(Location location) { this.location = location; }
    public void setStatus(String status) { this.status = status; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public void setRating(double rating) { this.rating = rating; }
    public void setCity(String city) { this.city = city; }
    
    public static class Builder {
        private String driverId;
        private String eventType;
        private long timestamp;
        private Location location;
        private String status;
        private String vehicleType;
        private double rating;
        private String city;
        
        public Builder driverId(String driverId) {
            this.driverId = driverId;
            return this;
        }
        
        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder location(Location location) {
            this.location = location;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder vehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }
        
        public Builder rating(double rating) {
            this.rating = rating;
            return this;
        }
        
        public Builder city(String city) {
            this.city = city;
            return this;
        }
        
        public DriverEvent build() {
            return new DriverEvent(this);
        }
    }
    
    @Override
    public String toString() {
        return "DriverEvent{" +
                "driverId='" + driverId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", location=" + location +
                ", status='" + status + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", rating=" + rating +
                ", city='" + city + '\'' +
                '}';
    }
}