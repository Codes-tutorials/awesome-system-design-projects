package com.example.vendingmachine;

import com.example.vendingmachine.service.InventoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.example.vendingmachine.model")
@EnableJpaRepositories(basePackages = "com.example.vendingmachine.repository")
public class VendingMachineApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendingMachineApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(InventoryService inventoryService) {
        return (args) -> {
            inventoryService.addProduct("Coke", 1.50, 10, "A1");
            inventoryService.addProduct("Chips", 1.00, 10, "B1");
            inventoryService.addProduct("Chocolate", 2.00, 5, "C1");
        };
    }
}
