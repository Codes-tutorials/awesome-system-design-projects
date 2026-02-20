package com.fooddelivery.controller;

import com.fooddelivery.model.MenuItem;
import com.fooddelivery.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for MenuItem management operations
 */
@RestController
@RequestMapping("/api/menu-items")
@Tag(name = "Menu Item Management", description = "APIs for menu item operations and management")
public class MenuItemController {
    
    @Autowired
    private MenuItemService menuItemService;
    
    @PostMapping
    @Operation(summary = "Create new menu item", description = "Add a new menu item to restaurant")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#menuItem.restaurant.id, authentication.principal.id))")
    public ResponseEntity<MenuItem> createMenuItem(@Valid @RequestBody MenuItem menuItem) {
        try {
            MenuItem savedMenuItem = menuItemService.createMenuItem(menuItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMenuItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get menu item by ID", description = "Retrieve menu item details by ID")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        return menuItemService.findById(id)
                .map(menuItem -> ResponseEntity.ok(menuItem))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get all menu items", description = "Retrieve all menu items with pagination")
    public ResponseEntity<Page<MenuItem>> getAllMenuItems(Pageable pageable) {
        Page<MenuItem> menuItems = menuItemService.findAll(pageable);
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get restaurant menu items", description = "Retrieve all menu items for a specific restaurant")
    public ResponseEntity<List<MenuItem>> getRestaurantMenuItems(@PathVariable Long restaurantId) {
        List<MenuItem> menuItems = menuItemService.findByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/restaurant/{restaurantId}/available")
    @Operation(summary = "Get available menu items", description = "Retrieve available menu items for a restaurant")
    public ResponseEntity<List<MenuItem>> getAvailableMenuItems(@PathVariable Long restaurantId) {
        List<MenuItem> menuItems = menuItemService.findAvailableByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/restaurant/{restaurantId}/category/{category}")
    @Operation(summary = "Get menu items by category", description = "Retrieve menu items by restaurant and category")
    public ResponseEntity<List<MenuItem>> getMenuItemsByCategory(@PathVariable Long restaurantId,
                                                               @PathVariable String category) {
        List<MenuItem> menuItems = menuItemService.findByRestaurantIdAndCategory(restaurantId, category);
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search menu items", description = "Search menu items by name or description")
    public ResponseEntity<List<MenuItem>> searchMenuItems(@RequestParam String query,
                                                        @RequestParam(required = false) Long restaurantId) {
        List<MenuItem> menuItems;
        if (restaurantId != null) {
            menuItems = menuItemService.searchByRestaurant(restaurantId, query);
        } else {
            menuItems = menuItemService.searchMenuItems(query);
        }
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/vegetarian")
    @Operation(summary = "Get vegetarian menu items", description = "Retrieve all vegetarian menu items")
    public ResponseEntity<List<MenuItem>> getVegetarianMenuItems(
            @RequestParam(required = false) Long restaurantId) {
        List<MenuItem> menuItems;
        if (restaurantId != null) {
            menuItems = menuItemService.findVegetarianByRestaurant(restaurantId);
        } else {
            menuItems = menuItemService.findVegetarianItems();
        }
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/vegan")
    @Operation(summary = "Get vegan menu items", description = "Retrieve all vegan menu items")
    public ResponseEntity<List<MenuItem>> getVeganMenuItems(
            @RequestParam(required = false) Long restaurantId) {
        List<MenuItem> menuItems;
        if (restaurantId != null) {
            menuItems = menuItemService.findVeganByRestaurant(restaurantId);
        } else {
            menuItems = menuItemService.findVeganItems();
        }
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular menu items", description = "Retrieve most popular menu items")
    public ResponseEntity<List<MenuItem>> getPopularMenuItems(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(defaultValue = "10") int limit) {
        List<MenuItem> menuItems;
        if (restaurantId != null) {
            menuItems = menuItemService.findPopularByRestaurant(restaurantId, limit);
        } else {
            menuItems = menuItemService.findPopularItems(limit);
        }
        return ResponseEntity.ok(menuItems);
    }
    
    @GetMapping("/price-range")
    @Operation(summary = "Get menu items by price range", description = "Retrieve menu items within specified price range")
    public ResponseEntity<List<MenuItem>> getMenuItemsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(required = false) Long restaurantId) {
        List<MenuItem> menuItems;
        if (restaurantId != null) {
            menuItems = menuItemService.findByPriceRangeAndRestaurant(minPrice, maxPrice, restaurantId);
        } else {
            menuItems = menuItemService.findByPriceRange(minPrice, maxPrice);
        }
        return ResponseEntity.ok(menuItems);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update menu item", description = "Update menu item information")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @menuItemService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<MenuItem> updateMenuItem(@PathVariable Long id,
                                                 @Valid @RequestBody MenuItem menuItem) {
        try {
            menuItem.setId(id);
            MenuItem updatedMenuItem = menuItemService.updateMenuItem(menuItem);
            return ResponseEntity.ok(updatedMenuItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/price")
    @Operation(summary = "Update menu item price", description = "Update the price of a menu item")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @menuItemService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<MenuItem> updateMenuItemPrice(@PathVariable Long id,
                                                      @RequestParam BigDecimal price) {
        try {
            MenuItem menuItem = menuItemService.updatePrice(id, price);
            return ResponseEntity.ok(menuItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/availability")
    @Operation(summary = "Update menu item availability", description = "Enable or disable menu item availability")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @menuItemService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<MenuItem> updateMenuItemAvailability(@PathVariable Long id,
                                                             @RequestParam Boolean isAvailable) {
        try {
            MenuItem menuItem = menuItemService.updateAvailability(id, isAvailable);
            return ResponseEntity.ok(menuItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/featured")
    @Operation(summary = "Update featured status", description = "Mark or unmark menu item as featured")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @menuItemService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<MenuItem> updateFeaturedStatus(@PathVariable Long id,
                                                       @RequestParam Boolean isFeatured) {
        try {
            MenuItem menuItem = menuItemService.updateFeaturedStatus(id, isFeatured);
            return ResponseEntity.ok(menuItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/rating")
    @Operation(summary = "Rate menu item", description = "Submit rating for menu item")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> rateMenuItem(@PathVariable Long id,
                                                          @RequestParam Integer rating,
                                                          @RequestParam(required = false) String review) {
        try {
            menuItemService.addRating(id, rating, review);
            return ResponseEntity.ok(Map.of("message", "Rating submitted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get menu item statistics", description = "Retrieve menu item performance statistics")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @menuItemService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Map<String, Object>> getMenuItemStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> statistics = menuItemService.getMenuItemStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/restaurant/{restaurantId}/categories")
    @Operation(summary = "Get menu categories", description = "Retrieve all menu categories for a restaurant")
    public ResponseEntity<List<String>> getMenuCategories(@PathVariable Long restaurantId) {
        List<String> categories = menuItemService.getMenuCategories(restaurantId);
        return ResponseEntity.ok(categories);
    }
    
    @PostMapping("/bulk-update-availability")
    @Operation(summary = "Bulk update availability", description = "Update availability for multiple menu items")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Map<String, String>> bulkUpdateAvailability(
            @RequestParam List<Long> menuItemIds,
            @RequestParam Boolean isAvailable) {
        try {
            menuItemService.bulkUpdateAvailability(menuItemIds, isAvailable);
            return ResponseEntity.ok(Map.of("message", "Menu items updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete menu item", description = "Permanently delete menu item")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @menuItemService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Map<String, String>> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemService.deleteMenuItem(id);
            return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}