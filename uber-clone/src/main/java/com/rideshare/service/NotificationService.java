package com.rideshare.service;

import com.rideshare.dto.LocationUpdateRequest;
import com.rideshare.model.Driver;
import com.rideshare.model.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending notifications to users
 * Handles push notifications, SMS, email, and real-time WebSocket messages
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // In a real implementation, you would inject:
    // - Firebase Cloud Messaging service for push notifications
    // - Twilio service for SMS
    // - Email service for email notifications
    
    /**
     * Send trip request notification to driver
     */
    public void sendTripRequestToDriver(Trip trip, Driver driver) {
        logger.info("Sending trip request to driver: {} for trip: {}", driver.getId(), trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRIP_REQUEST");
        notification.put("tripId", trip.getTripId());
        notification.put("pickupAddress", trip.getPickupAddress());
        notification.put("destinationAddress", trip.getDestinationAddress());
        notification.put("estimatedFare", trip.getTotalFare());
        notification.put("estimatedDistance", trip.getEstimatedDistanceKm());
        notification.put("riderName", trip.getRider().getFirstName());
        notification.put("riderRating", getRiderRating(trip.getRider()));
        
        // Send real-time notification via WebSocket
        sendWebSocketNotification(driver.getUser().getId(), notification);
        
        // Send push notification
        sendPushNotification(driver.getUser().getId(), 
            "New Trip Request", 
            "Trip to " + trip.getDestinationAddress() + " - $" + trip.getTotalFare());
        
        // Publish to Kafka for analytics
        publishNotificationEvent("TRIP_REQUEST_SENT", driver.getId(), trip.getTripId());
    }
    
    /**
     * Send driver assigned notification to rider
     */
    public void sendDriverAssignedToRider(Trip trip) {
        logger.info("Sending driver assigned notification to rider for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "DRIVER_ASSIGNED");
        notification.put("tripId", trip.getTripId());
        notification.put("driverName", trip.getDriver().getUser().getFirstName() + " " + 
                                     trip.getDriver().getUser().getLastName());
        notification.put("driverRating", trip.getDriver().getAverageRating());
        notification.put("driverPhone", trip.getDriver().getUser().getPhoneNumber());
        notification.put("vehicleInfo", getVehicleInfo(trip));
        notification.put("estimatedArrival", calculateEstimatedArrival(trip));
        
        // Send real-time notification
        sendWebSocketNotification(trip.getRider().getId(), notification);
        
        // Send push notification
        sendPushNotification(trip.getRider().getId(),
            "Driver Found!",
            trip.getDriver().getUser().getFirstName() + " is on the way");
        
        // Send SMS with driver details
        sendSmsNotification(trip.getRider().getPhoneNumber(),
            "Your driver " + trip.getDriver().getUser().getFirstName() + 
            " is on the way. Vehicle: " + getVehicleInfo(trip));
    }
    
    /**
     * Send driver arrived notification to rider
     */
    public void sendDriverArrivedToRider(Trip trip) {
        logger.info("Sending driver arrived notification for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "DRIVER_ARRIVED");
        notification.put("tripId", trip.getTripId());
        notification.put("message", "Your driver has arrived");
        
        sendWebSocketNotification(trip.getRider().getId(), notification);
        
        sendPushNotification(trip.getRider().getId(),
            "Driver Arrived",
            "Your driver has arrived at the pickup location");
    }
    
    /**
     * Send trip started notification to rider
     */
    public void sendTripStartedToRider(Trip trip) {
        logger.info("Sending trip started notification for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRIP_STARTED");
        notification.put("tripId", trip.getTripId());
        notification.put("message", "Your trip has started");
        
        sendWebSocketNotification(trip.getRider().getId(), notification);
        
        sendPushNotification(trip.getRider().getId(),
            "Trip Started",
            "Your trip to " + trip.getDestinationAddress() + " has started");
    }
    
    /**
     * Send trip completed notification to rider
     */
    public void sendTripCompletedToRider(Trip trip) {
        logger.info("Sending trip completed notification for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRIP_COMPLETED");
        notification.put("tripId", trip.getTripId());
        notification.put("totalFare", trip.getTotalFare());
        notification.put("distance", trip.getActualDistanceKm());
        notification.put("duration", trip.getActualDurationMinutes());
        
        sendWebSocketNotification(trip.getRider().getId(), notification);
        
        sendPushNotification(trip.getRider().getId(),
            "Trip Completed",
            "Your trip is complete. Total fare: $" + trip.getTotalFare());
        
        // Send receipt via email
        sendEmailReceipt(trip);
    }
    
    /**
     * Send trip cancelled notification
     */
    public void sendTripCancelledToRider(Trip trip) {
        logger.info("Sending trip cancelled notification to rider for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRIP_CANCELLED");
        notification.put("tripId", trip.getTripId());
        notification.put("reason", trip.getCancellationReason());
        notification.put("cancelledBy", trip.getCancelledBy());
        
        sendWebSocketNotification(trip.getRider().getId(), notification);
        
        sendPushNotification(trip.getRider().getId(),
            "Trip Cancelled",
            "Your trip has been cancelled. " + trip.getCancellationReason());
    }
    
    /**
     * Send trip cancelled notification to driver
     */
    public void sendTripCancelledToDriver(Trip trip) {
        logger.info("Sending trip cancelled notification to driver for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TRIP_CANCELLED");
        notification.put("tripId", trip.getTripId());
        notification.put("reason", trip.getCancellationReason());
        
        sendWebSocketNotification(trip.getDriver().getUser().getId(), notification);
        
        sendPushNotification(trip.getDriver().getUser().getId(),
            "Trip Cancelled",
            "The trip has been cancelled by the rider");
    }
    
    /**
     * Send no drivers available notification
     */
    public void sendNoDriversAvailableToRider(Trip trip) {
        logger.info("Sending no drivers available notification for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NO_DRIVERS_AVAILABLE");
        notification.put("tripId", trip.getTripId());
        notification.put("message", "No drivers available in your area. We're still looking...");
        
        sendWebSocketNotification(trip.getRider().getId(), notification);
        
        sendPushNotification(trip.getRider().getId(),
            "Looking for Drivers",
            "No drivers available right now. We're expanding our search...");
    }
    
    /**
     * Send system error notification
     */
    public void sendSystemErrorToRider(Trip trip) {
        logger.info("Sending system error notification for trip: {}", trip.getTripId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "SYSTEM_ERROR");
        notification.put("tripId", trip.getTripId());
        notification.put("message", "We're experiencing technical difficulties. Please try again.");
        
        sendWebSocketNotification(trip.getRider().getId(), notification);
        
        sendPushNotification(trip.getRider().getId(),
            "Technical Issue",
            "We're experiencing technical difficulties. Please try booking again.");
    }
    
    /**
     * Send real-time location update to rider
     */
    public void sendLocationUpdateToRider(Trip trip, LocationUpdateRequest locationUpdate) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "LOCATION_UPDATE");
        notification.put("tripId", trip.getTripId());
        notification.put("latitude", locationUpdate.getLatitude());
        notification.put("longitude", locationUpdate.getLongitude());
        notification.put("heading", locationUpdate.getHeading());
        notification.put("speed", locationUpdate.getSpeed());
        
        sendWebSocketNotification(trip.getRider().getId(), notification);
    }
    
    /**
     * Send earnings update to driver
     */
    public void sendEarningsUpdateToDriver(Long driverId, Map<String, Object> earningsData) {
        logger.debug("Sending earnings update to driver: {}", driverId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "EARNINGS_UPDATE");
        notification.putAll(earningsData);
        
        sendWebSocketNotification(driverId, notification);
    }
    
    /**
     * Send promotional notification
     */
    public void sendPromotionalNotification(Long userId, String title, String message) {
        logger.info("Sending promotional notification to user: {}", userId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PROMOTIONAL");
        notification.put("title", title);
        notification.put("message", message);
        
        sendWebSocketNotification(userId, notification);
        sendPushNotification(userId, title, message);
    }
    
    // Private helper methods
    
    private void sendWebSocketNotification(Long userId, Map<String, Object> notification) {
        try {
            String destination = "/user/" + userId + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            logger.debug("WebSocket notification sent to user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification to user: {}", userId, e);
        }
    }
    
    private void sendPushNotification(Long userId, String title, String message) {
        try {
            // In a real implementation, this would use Firebase Cloud Messaging
            logger.debug("Push notification sent to user {}: {} - {}", userId, title, message);
            
            // Simulate push notification by publishing to Kafka
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("userId", userId);
            pushData.put("title", title);
            pushData.put("message", message);
            pushData.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("push-notifications", pushData);
        } catch (Exception e) {
            logger.error("Failed to send push notification to user: {}", userId, e);
        }
    }
    
    private void sendSmsNotification(String phoneNumber, String message) {
        try {
            // In a real implementation, this would use Twilio or similar service
            logger.debug("SMS sent to {}: {}", phoneNumber, message);
            
            // Simulate SMS by publishing to Kafka
            Map<String, Object> smsData = new HashMap<>();
            smsData.put("phoneNumber", phoneNumber);
            smsData.put("message", message);
            smsData.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("sms-notifications", smsData);
        } catch (Exception e) {
            logger.error("Failed to send SMS to: {}", phoneNumber, e);
        }
    }
    
    private void sendEmailReceipt(Trip trip) {
        try {
            // In a real implementation, this would send an email receipt
            logger.debug("Email receipt sent for trip: {}", trip.getTripId());
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("email", trip.getRider().getEmail());
            emailData.put("tripId", trip.getTripId());
            emailData.put("totalFare", trip.getTotalFare());
            emailData.put("distance", trip.getActualDistanceKm());
            emailData.put("duration", trip.getActualDurationMinutes());
            emailData.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("email-receipts", emailData);
        } catch (Exception e) {
            logger.error("Failed to send email receipt for trip: {}", trip.getTripId(), e);
        }
    }
    
    private void publishNotificationEvent(String eventType, Long targetId, String tripId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("targetId", targetId);
            event.put("tripId", tripId);
            event.put("timestamp", System.currentTimeMillis());
            
            kafkaTemplate.send("notification-events", event);
        } catch (Exception e) {
            logger.error("Failed to publish notification event: {}", eventType, e);
        }
    }
    
    private String getVehicleInfo(Trip trip) {
        if (trip.getVehicle() != null) {
            return trip.getVehicle().getMake() + " " + 
                   trip.getVehicle().getModel() + " - " + 
                   trip.getVehicle().getLicensePlate();
        }
        return "Vehicle info not available";
    }
    
    private Integer calculateEstimatedArrival(Trip trip) {
        // In a real implementation, this would calculate based on driver's current location
        // and traffic conditions
        return 5; // Mock 5 minutes
    }
    
    private Double getRiderRating(com.rideshare.model.User rider) {
        // In a real implementation, this would fetch rider's rating
        return 4.8; // Mock rating
    }
}