package com.example.vendingmachine.service;

import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.model.Product;
import com.example.vendingmachine.repository.InventoryRepository;
import com.example.vendingmachine.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public void addProduct(String name, double price, int quantity, String location) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(quantity);
        inventory.setLocationCode(location);
        inventoryRepository.save(inventory);
    }

    public boolean hasSufficientStock(Long productId) {
        Product product = getProduct(productId);
        Optional<Inventory> inventory = inventoryRepository.findByProduct(product);
        return inventory.isPresent() && inventory.get().getQuantity() > 0;
    }

    @Transactional
    public void reduceStock(Long productId) {
        Product product = getProduct(productId);
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not initialized for product"));
        
        if (inventory.getQuantity() <= 0) {
            throw new RuntimeException("Out of stock");
        }
        inventory.setQuantity(inventory.getQuantity() - 1);
        inventoryRepository.save(inventory);
    }
}
