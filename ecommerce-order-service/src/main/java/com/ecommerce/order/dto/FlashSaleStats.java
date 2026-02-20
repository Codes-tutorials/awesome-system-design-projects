package com.ecommerce.order.dto;

import java.time.LocalDateTime;

public class FlashSaleStats {
    
    private String flashSaleId;
    private Long totalOrders;
    private Long successfulOrders;
    private Long failedOrders;
    private Long queuedOrders;
    private Double totalRevenue;
    private Double averageOrderValue;
    private Long peakOrdersPerSecond;
    private Long currentQueueSize;
    private Boolean isActive;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double conversionRate;
    private Long uniqueUsers;
    
    // Constructors
    public FlashSaleStats() {}
    
    private FlashSaleStats(Builder builder) {
        this.flashSaleId = builder.flashSaleId;
        this.totalOrders = builder.totalOrders;
        this.successfulOrders = builder.successfulOrders;
        this.failedOrders = builder.failedOrders;
        this.queuedOrders = builder.queuedOrders;
        this.totalRevenue = builder.totalRevenue;
        this.averageOrderValue = builder.averageOrderValue;
        this.peakOrdersPerSecond = builder.peakOrdersPerSecond;
        this.currentQueueSize = builder.currentQueueSize;
        this.isActive = builder.isActive;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.conversionRate = builder.conversionRate;
        this.uniqueUsers = builder.uniqueUsers;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String flashSaleId;
        private Long totalOrders;
        private Long successfulOrders;
        private Long failedOrders;
        private Long queuedOrders;
        private Double totalRevenue;
        private Double averageOrderValue;
        private Long peakOrdersPerSecond;
        private Long currentQueueSize;
        private Boolean isActive;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double conversionRate;
        private Long uniqueUsers;
        
        public Builder flashSaleId(String flashSaleId) {
            this.flashSaleId = flashSaleId;
            return this;
        }
        
        public Builder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }
        
        public Builder successfulOrders(Long successfulOrders) {
            this.successfulOrders = successfulOrders;
            return this;
        }
        
        public Builder failedOrders(Long failedOrders) {
            this.failedOrders = failedOrders;
            return this;
        }
        
        public Builder queuedOrders(Long queuedOrders) {
            this.queuedOrders = queuedOrders;
            return this;
        }
        
        public Builder totalRevenue(Double totalRevenue) {
            this.totalRevenue = totalRevenue;
            return this;
        }
        
        public Builder averageOrderValue(Double averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
            return this;
        }
        
        public Builder peakOrdersPerSecond(Long peakOrdersPerSecond) {
            this.peakOrdersPerSecond = peakOrdersPerSecond;
            return this;
        }
        
        public Builder currentQueueSize(Long currentQueueSize) {
            this.currentQueueSize = currentQueueSize;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }
        
        public Builder conversionRate(Double conversionRate) {
            this.conversionRate = conversionRate;
            return this;
        }
        
        public Builder uniqueUsers(Long uniqueUsers) {
            this.uniqueUsers = uniqueUsers;
            return this;
        }
        
        public FlashSaleStats build() {
            return new FlashSaleStats(this);
        }
    }
    
    // Getters and Setters
    public String getFlashSaleId() {
        return flashSaleId;
    }
    
    public void setFlashSaleId(String flashSaleId) {
        this.flashSaleId = flashSaleId;
    }
    
    public Long getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public Long getSuccessfulOrders() {
        return successfulOrders;
    }
    
    public void setSuccessfulOrders(Long successfulOrders) {
        this.successfulOrders = successfulOrders;
    }
    
    public Long getFailedOrders() {
        return failedOrders;
    }
    
    public void setFailedOrders(Long failedOrders) {
        this.failedOrders = failedOrders;
    }
    
    public Long getQueuedOrders() {
        return queuedOrders;
    }
    
    public void setQueuedOrders(Long queuedOrders) {
        this.queuedOrders = queuedOrders;
    }
    
    public Double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public Double getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public Long getPeakOrdersPerSecond() {
        return peakOrdersPerSecond;
    }
    
    public void setPeakOrdersPerSecond(Long peakOrdersPerSecond) {
        this.peakOrdersPerSecond = peakOrdersPerSecond;
    }
    
    public Long getCurrentQueueSize() {
        return currentQueueSize;
    }
    
    public void setCurrentQueueSize(Long currentQueueSize) {
        this.currentQueueSize = currentQueueSize;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Double getConversionRate() {
        return conversionRate;
    }
    
    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }
    
    public Long getUniqueUsers() {
        return uniqueUsers;
    }
    
    public void setUniqueUsers(Long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }
    
    // Helper methods
    public Double getSuccessRate() {
        if (totalOrders == null || totalOrders == 0) {
            return 0.0;
        }
        return (successfulOrders != null ? successfulOrders.doubleValue() : 0.0) / totalOrders.doubleValue() * 100.0;
    }
    
    public Double getFailureRate() {
        if (totalOrders == null || totalOrders == 0) {
            return 0.0;
        }
        return (failedOrders != null ? failedOrders.doubleValue() : 0.0) / totalOrders.doubleValue() * 100.0;
    }
    
    public Long getPendingOrders() {
        return queuedOrders != null ? queuedOrders : 0L;
    }
    
    @Override
    public String toString() {
        return "FlashSaleStats{" +
                "flashSaleId='" + flashSaleId + '\'' +
                ", totalOrders=" + totalOrders +
                ", successfulOrders=" + successfulOrders +
                ", failedOrders=" + failedOrders +
                ", queuedOrders=" + queuedOrders +
                ", totalRevenue=" + totalRevenue +
                ", averageOrderValue=" + averageOrderValue +
                ", peakOrdersPerSecond=" + peakOrdersPerSecond +
                ", currentQueueSize=" + currentQueueSize +
                ", isActive=" + isActive +
                ", successRate=" + getSuccessRate() + "%" +
                '}';
    }
}