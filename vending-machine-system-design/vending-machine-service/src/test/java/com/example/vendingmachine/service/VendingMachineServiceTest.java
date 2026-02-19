package com.example.vendingmachine.service;

import com.example.vendingmachine.dto.DispenseResponse;
import com.example.vendingmachine.model.Coin;
import com.example.vendingmachine.model.Product;
import com.example.vendingmachine.model.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendingMachineServiceTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private VendingMachineService vendingService;

    @Test
    void testFullFlow() {
        // 1. Init Session
        String sessionId = vendingService.initSession();
        assertNotNull(sessionId);

        // 2. Select Product
        Long productId = 1L;
        Product product = new Product(1L, "Coke", 1.50);
        
        when(inventoryService.hasSufficientStock(productId)).thenReturn(true);
        when(inventoryService.getProduct(productId)).thenReturn(product);

        vendingService.selectProduct(sessionId, productId);

        // 3. Insert Money
        vendingService.insertNote(sessionId, Note.ONE); // 1.00
        vendingService.insertCoin(sessionId, Coin.QUARTER); // 1.25
        vendingService.insertCoin(sessionId, Coin.QUARTER); // 1.50

        // 4. Dispense
        DispenseResponse response = vendingService.dispense(sessionId);
        
        assertEquals("Dispensing Coke", response.getMessage());
        assertEquals(0.0, response.getChange());
        verify(inventoryService).reduceStock(productId);
    }

    @Test
    void testInsufficientFunds() {
        String sessionId = vendingService.initSession();
        Long productId = 1L;
        Product product = new Product(1L, "Coke", 1.50);
        
        when(inventoryService.hasSufficientStock(productId)).thenReturn(true);
        when(inventoryService.getProduct(productId)).thenReturn(product);

        vendingService.selectProduct(sessionId, productId);
        vendingService.insertNote(sessionId, Note.ONE); // 1.00

        Exception exception = assertThrows(RuntimeException.class, () -> {
            vendingService.dispense(sessionId);
        });

        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }
}
