package com.example.cache.controller;

import com.example.cache.model.Product;
import com.example.cache.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Product createProduct(@RequestParam String name, @RequestParam BigDecimal price, @RequestParam int stock) {
        return productService.createProduct(name, price, stock);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    // Scenario 1: The Problem - Data becomes stale
    @PutMapping("/{id}/bad")
    public Product updateBad(@PathVariable Long id, @RequestParam BigDecimal price) {
        return productService.updateProductBad(id, price);
    }

    // Scenario 2: The Standard Fix - Cache Aside / Eviction
    @PutMapping("/{id}/evict")
    public Product updateEvict(@PathVariable Long id, @RequestParam BigDecimal price) {
        return productService.updateProductEvict(id, price);
    }

    // Scenario 3: High Concurrency Fix - Delayed Double Deletion (Simulated)
    @PutMapping("/{id}/double-delete")
    public Product updateDoubleDelete(@PathVariable Long id, @RequestParam BigDecimal price) {
        return productService.updateProductDoubleDelete(id, price);
    }

    // Scenario 4: Eventual Consistency - Async / CDC Pattern
    @PutMapping("/{id}/async")
    public Product updateAsync(@PathVariable Long id, @RequestParam BigDecimal price) {
        return productService.updateProductAsync(id, price);
    }
}
