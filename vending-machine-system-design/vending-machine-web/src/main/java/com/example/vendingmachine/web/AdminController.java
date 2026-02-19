package com.example.vendingmachine.web;

import com.example.vendingmachine.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final InventoryService inventoryService;

    @PostMapping("/product")
    @Operation(summary = "Add a new product")
    public ResponseEntity<String> addProduct(@RequestParam String name, @RequestParam double price, @RequestParam int quantity, @RequestParam String location) {
        inventoryService.addProduct(name, price, quantity, location);
        return ResponseEntity.ok("Product added");
    }
}
