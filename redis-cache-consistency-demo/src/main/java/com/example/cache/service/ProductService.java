package com.example.cache.service;

import com.example.cache.event.ProductUpdatedEvent;
import com.example.cache.model.Product;
import com.example.cache.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheManager cacheManager;

    // --- 1. Read Path (Standard Cache-Aside) ---
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        System.out.println("Fetching from DB for ID: " + id);
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // --- 2. Bad Strategy: Update DB but forget/fail to update Cache ---
    @Transactional
    public Product updateProductBad(Long id, BigDecimal newPrice) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setPrice(newPrice);
        return productRepository.save(product);
        // CACHE IS NOW STALE!
    }

    // --- 3. Good Strategy: Cache Eviction (Write-Around) ---
    // This deletes the cache entry, forcing the next read to re-fetch from DB.
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public Product updateProductEvict(Long id, BigDecimal newPrice) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setPrice(newPrice);
        return productRepository.save(product);
    }
    
    // --- 4. Better Strategy for Consistency: Delayed Double Deletion (Simulated) ---
    // Useful for high concurrency to handle race conditions where a read happens during write.
    // Ideally this is done via a separate thread or scheduled task.
    @Transactional
    public Product updateProductDoubleDelete(Long id, BigDecimal newPrice) {
        // 1. Delete Cache (First Deletion)
        Objects.requireNonNull(cacheManager.getCache("products")).evict(id);
        
        Product product = productRepository.findById(id).orElseThrow();
        product.setPrice(newPrice);
        Product saved = productRepository.save(product);
        
        // 2. Publish event to delete again after a delay (Second Deletion - Simulated here via the async listener)
        eventPublisher.publishEvent(new ProductUpdatedEvent(id));
        
        return saved;
    }

    // --- 5. Async Invalidation (CDC Simulation) ---
    // Decouples DB update from Cache logic.
    // The DB transaction commits, then an event (like a binlog) triggers cache eviction.
    @Transactional
    public Product updateProductAsync(Long id, BigDecimal newPrice) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setPrice(newPrice);
        Product saved = productRepository.save(product);
        
        // In a real CDC system, Debezium would pick this up.
        // We simulate it by publishing an event.
        eventPublisher.publishEvent(new ProductUpdatedEvent(id));
        
        return saved;
    }
    
    @Transactional
    public Product createProduct(String name, BigDecimal price, int stock) {
        return productRepository.save(Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stock)
                .build());
    }
}
