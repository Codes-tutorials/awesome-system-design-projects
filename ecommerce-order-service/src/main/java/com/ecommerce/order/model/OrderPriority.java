package com.ecommerce.order.model;

/**
 * Enumeration of order priorities for processing queue management
 */
public enum OrderPriority {
    LOW(1, "low", "Low priority order"),
    NORMAL(2, "normal", "Normal priority order"),
    HIGH(3, "high", "High priority order"),
    URGENT(4, "urgent", "Urgent order requiring immediate processing"),
    FLASH_SALE(5, "flash_sale", "Flash sale order with highest priority");
    
    private final int level;
    private final String code;
    private final String description;
    
    OrderPriority(int level, String code, String description) {
        this.level = level;
        this.code = code;
        this.description = description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static OrderPriority fromCode(String code) {
        for (OrderPriority priority : values()) {
            if (priority.code.equals(code)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown order priority code: " + code);
    }
    
    public static OrderPriority fromLevel(int level) {
        for (OrderPriority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown order priority level: " + level);
    }
    
    /**
     * Check if this priority is higher than the given priority
     */
    public boolean isHigherThan(OrderPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Check if this priority requires immediate processing
     */
    public boolean requiresImmediateProcessing() {
        return this == HIGH || this == URGENT || this == FLASH_SALE;
    }
    
    /**
     * Check if this priority is for flash sale orders
     */
    public boolean isFlashSale() {
        return this == FLASH_SALE;
    }
    
    /**
     * Get processing timeout in milliseconds based on priority
     */
    public long getProcessingTimeoutMs() {
        return switch (this) {
            case FLASH_SALE -> 500L;  // 500ms for flash sale
            case URGENT -> 1000L;     // 1 second for urgent
            case HIGH -> 2000L;       // 2 seconds for high
            case NORMAL -> 5000L;     // 5 seconds for normal
            case LOW -> 10000L;       // 10 seconds for low
        };
    }
    
    /**
     * Get queue weight for priority-based processing
     */
    public int getQueueWeight() {
        return switch (this) {
            case FLASH_SALE -> 100;
            case URGENT -> 50;
            case HIGH -> 20;
            case NORMAL -> 10;
            case LOW -> 1;
        };
    }
}