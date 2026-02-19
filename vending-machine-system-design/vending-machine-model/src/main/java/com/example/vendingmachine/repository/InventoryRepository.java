package com.example.vendingmachine.repository;

import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProduct(Product product);
    Optional<Inventory> findByLocationCode(String locationCode);
}
