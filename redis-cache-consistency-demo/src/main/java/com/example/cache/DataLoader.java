package com.example.cache;

import com.example.cache.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductService productService;

    @Override
    public void run(String... args) throws Exception {
        productService.createProduct("Gaming Mouse", new BigDecimal("49.99"), 100);
        productService.createProduct("Mechanical Keyboard", new BigDecimal("129.99"), 50);
        System.out.println("--- Data Loaded: Products ID 1 and 2 created ---");
    }
}
