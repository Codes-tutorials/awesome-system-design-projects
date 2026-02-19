package com.example.vendingmachine.web;

import com.example.vendingmachine.dto.DispenseResponse;
import com.example.vendingmachine.model.Coin;
import com.example.vendingmachine.model.Note;
import com.example.vendingmachine.service.VendingMachineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vending")
@RequiredArgsConstructor
public class VendingController {
    private final VendingMachineService vendingService;

    @PostMapping("/start")
    @Operation(summary = "Start a new session")
    public ResponseEntity<String> startSession() {
        return ResponseEntity.ok(vendingService.initSession());
    }

    @PostMapping("/{sessionId}/select/{productId}")
    @Operation(summary = "Select a product")
    public ResponseEntity<String> selectProduct(@PathVariable String sessionId, @PathVariable Long productId) {
        vendingService.selectProduct(sessionId, productId);
        return ResponseEntity.ok("Product selected");
    }

    @PostMapping("/{sessionId}/insert-coin")
    @Operation(summary = "Insert a coin (PENNY, NICKEL, DIME, QUARTER)")
    public ResponseEntity<String> insertCoin(@PathVariable String sessionId, @RequestParam Coin coin) {
        vendingService.insertCoin(sessionId, coin);
        return ResponseEntity.ok("Coin accepted");
    }

    @PostMapping("/{sessionId}/insert-note")
    @Operation(summary = "Insert a note (ONE, FIVE, TEN, TWENTY)")
    public ResponseEntity<String> insertNote(@PathVariable String sessionId, @RequestParam Note note) {
        vendingService.insertNote(sessionId, note);
        return ResponseEntity.ok("Note accepted");
    }

    @PostMapping("/{sessionId}/dispense")
    @Operation(summary = "Dispense product")
    public ResponseEntity<DispenseResponse> dispense(@PathVariable String sessionId) {
        return ResponseEntity.ok(vendingService.dispense(sessionId));
    }
}
