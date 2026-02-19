package com.example.cache.listener;

import com.example.cache.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CacheInvalidationListener {

    private final CacheManager cacheManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdate(ProductUpdatedEvent event) {
        System.out.println("Async Listener: Invalidating cache for Product ID: " + event.getProductId());
        
        // Simulate a small delay (like network lag or CDC latency)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Evict the cache
        if (cacheManager.getCache("products") != null) {
            cacheManager.getCache("products").evict(event.getProductId());
        }
    }
}
