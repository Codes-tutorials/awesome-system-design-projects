package com.ecommerce.order.model;

/**
 * Enumeration of order statuses in the order lifecycle
 */
public enum OrderStatus {
    PENDING("pending", "Order is pending confirmation"),
    CONFIRMED("confirmed", "Order has been confirmed"),
    PROCESSING("processing", "Order is being processed"),
    SHIPPED("shipped", "Order has been shipped"),
    DELIVERED("delivered", "Order has been delivered"),
    CANCELLED("cancelled", "Order has been cancelled"),
    REFUNDED("refunded", "Order has been refunded"),
    FAILED("failed", "Order processing failed");
    
    private final String code;
    private final String description;
    
    OrderStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }
    
    /**
     * Check if this status indicates a final state (no further processing needed)
     */
    public boolean isFinalState() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED || this == FAILED;
    }
    
    /**
     * Check if this status allows modification
     */
    public boolean isModifiable() {
        return this == PENDING || this == CONFIRMED;
    }
    
    /**
     * Check if this status allows cancellation
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PROCESSING;
    }
    
    /**
     * Check if this status indicates successful completion
     */
    public boolean isSuccessful() {
        return this == DELIVERED;
    }
    
    /**
     * Get the next valid statuses from current status
     */
    public OrderStatus[] getNextValidStatuses() {
        return switch (this) {
            case PENDING -> new OrderStatus[]{CONFIRMED, CANCELLED, FAILED};
            case CONFIRMED -> new OrderStatus[]{PROCESSING, CANCELLED};
            case PROCESSING -> new OrderStatus[]{SHIPPED, CANCELLED};
            case SHIPPED -> new OrderStatus[]{DELIVERED, CANCELLED};
            case DELIVERED -> new OrderStatus[]{REFUNDED};
            case CANCELLED, REFUNDED, FAILED -> new OrderStatus[]{};
        };
    }
    
    /**
     * Check if transition to target status is valid
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        OrderStatus[] validStatuses = getNextValidStatuses();
        for (OrderStatus status : validStatuses) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }
}