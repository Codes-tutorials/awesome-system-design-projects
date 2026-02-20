package com.uber.analytics.events;

import com.uber.analytics.flink.sources.RideEventSource.Location;
import java.io.Serializable;

/**
 * Ride event model class
 */
public class RideEvent implements Serializable {
    
    private String rideId;
    private String riderId;
    private String eventType;
    private long timestamp;
    private Location pickupLocation;
    private Location dropoffLocation;
    private String rideType;
    private double estimatedFare;
    private String city;
    
    // Default constructor for serialization
    public RideEvent() {}
    
    private RideEvent(Builder builder) {
        this.rideId = builder.rideId;
        this.riderId = builder.riderId;
        this.eventType = builder.eventType;
        this.timestamp = builder.timestamp;
        this.pickupLocation = builder.pickupLocation;
        this.dropoffLocation = builder.dropoffLocation;
        this.rideType = builder.rideType;
        this.estimatedFare = builder.estimatedFare;
        this.city = builder.city;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getRideId() { return rideId; }
    public String getRiderId() { return riderId; }
    public String getEventType() { return eventType; }
    public long getTimestamp() { return timestamp; }
    public Location getPickupLocation() { return pickupLocation; }
    public Location getDropoffLocation() { return dropoffLocation; }
    public String getRideType() { return rideType; }
    public double getEstimatedFare() { return estimatedFare; }
    public String getCity() { return city; }
    
    // Setters for serialization
    public void setRideId(String rideId) { this.rideId = rideId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setPickupLocation(Location pickupLocation) { this.pickupLocation = pickupLocation; }
    public void setDropoffLocation(Location dropoffLocation) { this.dropoffLocation = dropoffLocation; }
    public void setRideType(String rideType) { this.rideType = rideType; }
    public void setEstimatedFare(double estimatedFare) { this.estimatedFare = estimatedFare; }
    public void setCity(String city) { this.city = city; }
    
    public static class Builder {
        private String rideId;
        private String riderId;
        private String eventType;
        private long timestamp;
        private Location pickupLocation;
        private Location dropoffLocation;
        private String rideType;
        private double estimatedFare;
        private String city;
        
        public Builder rideId(String rideId) {
            this.rideId = rideId;
            return this;
        }
        
        public Builder riderId(String riderId) {
            this.riderId = riderId;
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
        
        public Builder pickupLocation(Location pickupLocation) {
            this.pickupLocation = pickupLocation;
            return this;
        }
        
        public Builder dropoffLocation(Location dropoffLocation) {
            this.dropoffLocation = dropoffLocation;
            return this;
        }
        
        public Builder rideType(String rideType) {
            this.rideType = rideType;
            return this;
        }
        
        public Builder estimatedFare(double estimatedFare) {
            this.estimatedFare = estimatedFare;
            return this;
        }
        
        public Builder city(String city) {
            this.city = city;
            return this;
        }
        
        public RideEvent build() {
            return new RideEvent(this);
        }
    }
    
    @Override
    public String toString() {
        return "RideEvent{" +
                "rideId='" + rideId + '\'' +
                ", riderId='" + riderId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", pickupLocation=" + pickupLocation +
                ", dropoffLocation=" + dropoffLocation +
                ", rideType='" + rideType + '\'' +
                ", estimatedFare=" + estimatedFare +
                ", city='" + city + '\'' +
                '}';
    }
}