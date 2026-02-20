package com.fooddelivery.service;

import com.fooddelivery.model.MenuItem;
import com.fooddelivery.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for MenuItem operations
 */
@Service
@Transactional
public class MenuItemService {
    
    @Autowired
    private MenuItemRepository menuItemRepository;
    
    public MenuItem createMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }
    
    public Optional<MenuItem> findById(Long id) {
        return menuItemRepository.findById(id);
    }
    
    public Page<MenuItem> findAll(Pageable pageable) {
        return menuItemRepository.findAll(pageable);
    }
    
    public List<MenuItem> findByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }
    
    public List<MenuItem> findAvailableByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
    }
    
    public List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, String category) {
        return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, category);
    }
    
    public List<MenuItem> searchMenuItems(String query) {
        return menuItemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }
    
    public List<MenuItem> searchByRestaurant(Long restaurantId, String query) {
        return menuItemRepository.findByRestaurantIdAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                restaurantId, query, query);
    }
    
    public List<MenuItem> findVegetarianItems() {
        return menuItemRepository.findByIsVegetarianTrue();
    }
    
    public List<MenuItem> findVegetarianByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsVegetarianTrue(restaurantId);
    }
    
    public List<MenuItem> findVeganItems() {
        return menuItemRepository.findByIsVeganTrue();
    }
    
    public List<MenuItem> findVeganByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndIsVeganTrue(restaurantId);
    }
    
    public List<MenuItem> findPopularItems(int limit) {
        // Implementation for finding popular items based on order count
        return menuItemRepository.findAll().stream().limit(limit).toList();
    }
    
    public List<MenuItem> findPopularByRestaurant(Long restaurantId, int limit) {
        // Implementation for finding popular items by restaurant
        return findByRestaurantId(restaurantId).stream().limit(limit).toList();
    }
    
    public List<MenuItem> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return menuItemRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public List<MenuItem> findByPriceRangeAndRestaurant(BigDecimal minPrice, BigDecimal maxPrice, Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndPriceBetween(restaurantId, minPrice, maxPrice);
    }
    
    public MenuItem updateMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }
    
    public MenuItem updatePrice(Long id, BigDecimal price) {
        MenuItem menuItem = findById(id).orElseThrow(() -> new RuntimeException("Menu item not found"));
        menuItem.setPrice(price);
        return menuItemRepository.save(menuItem);
    }
    
    public MenuItem updateAvailability(Long id, Boolean isAvailable) {
        MenuItem menuItem = findById(id).orElseThrow(() -> new RuntimeException("Menu item not found"));
        menuItem.setIsAvailable(isAvailable);
        return menuItemRepository.save(menuItem);
    }
    
    public MenuItem updateFeaturedStatus(Long id, Boolean isFeatured) {
        MenuItem menuItem = findById(id).orElseThrow(() -> new RuntimeException("Menu item not found"));
        menuItem.setIsFeatured(isFeatured);
        return menuItemRepository.save(menuItem);
    }
    
    public void addRating(Long id, Integer rating, String review) {
        // Implementation for adding rating to menu item
    }
    
    public Map<String, Object> getMenuItemStatistics(Long id) {
        // Implementation for menu item statistics
        return Map.of("statistics", "Not implemented yet");
    }
    
    public List<String> getMenuCategories(Long restaurantId) {
        return menuItemRepository.findDistinctCategoriesByRestaurantId(restaurantId);
    }
    
    public void bulkUpdateAvailability(List<Long> menuItemIds, Boolean isAvailable) {
        menuItemRepository.bulkUpdateAvailability(menuItemIds, isAvailable);
    }
    
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }
    
    public boolean isOwner(Long menuItemId, Long userId) {
        // Implementation to check if user owns the menu item's restaurant
        return true; // Placeholder
    }
}