package com.ecommerce.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    
    @Value("${order.processing.async-enabled:true}")
    private boolean asyncEnabled;
    
    @Value("${order.processing.batch-size:100}")
    private int batchSize;
    
    @Value("${order.flash-sale.processing-threads:20}")
    private int flashSaleThreads;
    
    /**
     * Main executor for order processing operations
     */
    @Bean(name = "orderProcessingExecutor")
    public Executor orderProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - always active threads
        executor.setCorePoolSize(10);
        
        // Maximum pool size - can grow up to this number
        executor.setMaxPoolSize(50);
        
        // Queue capacity - pending tasks queue size
        executor.setQueueCapacity(1000);
        
        // Thread name prefix for monitoring
        executor.setThreadNamePrefix("OrderProcessing-");
        
        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);
        
        // Rejection policy when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Dedicated executor for flash sale processing
     */
    @Bean(name = "flashSaleExecutor")
    public Executor flashSaleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Higher core pool size for flash sales
        executor.setCorePoolSize(flashSaleThreads);
        executor.setMaxPoolSize(flashSaleThreads * 2);
        
        // Larger queue for high-volume flash sales
        executor.setQueueCapacity(5000);
        
        executor.setThreadNamePrefix("FlashSale-");
        executor.setKeepAliveSeconds(30);
        
        // Abort policy for flash sales - fail fast if overwhelmed
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        
        executor.setAllowCoreThreadTimeOut(false); // Keep core threads alive
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor for payment processing operations
     */
    @Bean(name = "paymentProcessingExecutor")
    public Executor paymentProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Smaller pool for payment operations (external service calls)
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        
        executor.setThreadNamePrefix("PaymentProcessing-");
        executor.setKeepAliveSeconds(120);
        
        // Caller runs policy to ensure payment processing doesn't fail
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(45);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor for notification operations
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(1000);
        
        executor.setThreadNamePrefix("Notification-");
        executor.setKeepAliveSeconds(60);
        
        // Discard policy for notifications - non-critical operations
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(false); // Don't wait for notifications
        executor.setAwaitTerminationSeconds(10);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor for batch processing operations
     */
    @Bean(name = "batchProcessingExecutor")
    public Executor batchProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Optimized for batch operations
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        
        executor.setThreadNamePrefix("BatchProcessing-");
        executor.setKeepAliveSeconds(300); // Longer keep alive for batch jobs
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Longer wait for batch operations
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor for analytics and reporting operations
     */
    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Low priority, background processing
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        
        executor.setThreadNamePrefix("Analytics-");
        executor.setKeepAliveSeconds(600); // Long keep alive for background tasks
        
        // Discard policy - analytics are not critical
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(5);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Executor for cleanup operations
     */
    @Bean(name = "cleanupExecutor")
    public Executor cleanupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Single thread for cleanup operations
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        
        executor.setThreadNamePrefix("Cleanup-");
        executor.setKeepAliveSeconds(300);
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(10);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Custom task decorator for adding MDC context to async tasks
     */
    @Bean
    public org.springframework.core.task.TaskDecorator taskDecorator() {
        return new MdcTaskDecorator();
    }
    
    /**
     * Task decorator that preserves MDC context across async boundaries
     */
    public static class MdcTaskDecorator implements org.springframework.core.task.TaskDecorator {
        
        @Override
        public Runnable decorate(Runnable runnable) {
            // Capture MDC context from current thread
            java.util.Map<String, String> contextMap = org.slf4j.MDC.getCopyOfContextMap();
            
            return () -> {
                try {
                    // Set MDC context in async thread
                    if (contextMap != null) {
                        org.slf4j.MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    // Clear MDC context after execution
                    org.slf4j.MDC.clear();
                }
            };
        }
    }
}