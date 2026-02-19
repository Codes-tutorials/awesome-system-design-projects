package com.example.vendingmachine.service;

import com.example.vendingmachine.dto.DispenseResponse;
import com.example.vendingmachine.model.Coin;
import com.example.vendingmachine.model.Note;
import com.example.vendingmachine.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class VendingMachineService {
    private final InventoryService inventoryService;
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public String initSession() {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new UserSession(sessionId));
        return sessionId;
    }

    public UserSession getSession(String sessionId) {
        if (!sessions.containsKey(sessionId)) {
            throw new RuntimeException("Invalid Session");
        }
        return sessions.get(sessionId);
    }

    public void selectProduct(String sessionId, Long productId) {
        UserSession session = getSession(sessionId);
        if (!inventoryService.hasSufficientStock(productId)) {
            throw new RuntimeException("Product Out of Stock");
        }
        session.setSelectedProductId(productId);
    }

    public void insertCoin(String sessionId, Coin coin) {
        UserSession session = getSession(sessionId);
        session.setCurrentBalance(session.getCurrentBalance() + coin.getValue());
    }

    public void insertNote(String sessionId, Note note) {
        UserSession session = getSession(sessionId);
        session.setCurrentBalance(session.getCurrentBalance() + note.getValue());
    }

    public DispenseResponse dispense(String sessionId) {
        UserSession session = getSession(sessionId);
        if (session.getSelectedProductId() == null) {
            throw new RuntimeException("No product selected");
        }

        Product product = inventoryService.getProduct(session.getSelectedProductId());
        if (session.getCurrentBalance() < product.getPrice()) {
            throw new RuntimeException("Insufficient funds. Price: " + product.getPrice());
        }

        // Reduce stock
        inventoryService.reduceStock(product.getId());

        double change = session.getCurrentBalance() - product.getPrice();
        
        // Clear session or reset
        sessions.remove(sessionId); // End session after dispense

        return DispenseResponse.builder()
                .message("Dispensing " + product.getName())
                .productName(product.getName())
                .change(change)
                .build();
    }
    
    public void cancel(String sessionId) {
        sessions.remove(sessionId);
    }
}
