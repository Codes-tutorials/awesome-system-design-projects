package com.fooddelivery.controller;

import com.fooddelivery.model.CartItem;
import com.fooddelivery.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Cart management operations
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart Management", description = "APIs for shopping cart operations")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @PostMapping("/add")
    @Operation(summary = "Add item to cart", description = "Add menu item to user's cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartItem> addToCart(@RequestParam Long menuItemId,
                                            @RequestParam Integer quantity,
                                            @RequestParam(required = false) String specialInstructions,
                                            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartItem cartItem = cartService.addToCart(userId, menuItemId, quantity, specialInstructions);
            return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    @Operation(summary = "Get cart items", description = "Retrieve all items in user's cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CartItem>> getCartItems(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<CartItem> cartItems = cartService.getCartItems(userId);
        return ResponseEntity.ok(cartItems);
    }
    
    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get cart items by restaurant", description = "Retrieve cart items for specific restaurant")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CartItem>> getCartItemsByRestaurant(@PathVariable Long restaurantId,
                                                                 Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<CartItem> cartItems = cartService.getCartItemsByRestaurant(userId, restaurantId);
        return ResponseEntity.ok(cartItems);
    }
    
    @GetMapping("/summary")
    @Operation(summary = "Get cart summary", description = "Get cart summary with totals")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getCartSummary(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        Map<String, Object> summary = cartService.getCartSummary(userId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/summary/restaurant/{restaurantId}")
    @Operation(summary = "Get restaurant cart summary", description = "Get cart summary for specific restaurant")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getRestaurantCartSummary(@PathVariable Long restaurantId,
                                                                       Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        Map<String, Object> summary = cartService.getRestaurantCartSummary(userId, restaurantId);
        return ResponseEntity.ok(summary);
    }
    
    @PutMapping("/item/{cartItemId}")
    @Operation(summary = "Update cart item", description = "Update quantity or special instructions of cart item")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartItem> updateCartItem(@PathVariable Long cartItemId,
                                                 @RequestParam(required = false) Integer quantity,
                                                 @RequestParam(required = false) String specialInstructions,
                                                 Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartItem cartItem = cartService.updateCartItem(userId, cartItemId, quantity, specialInstructions);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/item/{cartItemId}/quantity")
    @Operation(summary = "Update item quantity", description = "Update quantity of specific cart item")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartItem> updateItemQuantity(@PathVariable Long cartItemId,
                                                     @RequestParam Integer quantity,
                                                     Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartItem cartItem = cartService.updateItemQuantity(userId, cartItemId, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/item/{cartItemId}")
    @Operation(summary = "Remove cart item", description = "Remove specific item from cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> removeCartItem(@PathVariable Long cartItemId,
                                                            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            cartService.removeCartItem(userId, cartItemId);
            return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Clear restaurant cart", description = "Remove all items from cart for specific restaurant")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> clearRestaurantCart(@PathVariable Long restaurantId,
                                                                 Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            cartService.clearRestaurantCart(userId, restaurantId);
            return ResponseEntity.ok(Map.of("message", "Restaurant cart cleared"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire cart", description = "Remove all items from user's cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> clearCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            cartService.clearCart(userId);
            return ResponseEntity.ok(Map.of("message", "Cart cleared"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/count")
    @Operation(summary = "Get cart item count", description = "Get total number of items in cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Integer>> getCartItemCount(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        Integer count = cartService.getCartItemCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @GetMapping("/total")
    @Operation(summary = "Get cart total", description = "Get total amount of items in cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, BigDecimal>> getCartTotal(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        BigDecimal total = cartService.getCartTotal(userId);
        return ResponseEntity.ok(Map.of("total", total));
    }
    
    @GetMapping("/restaurants")
    @Operation(summary = "Get cart restaurants", description = "Get list of restaurants that have items in cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<Map<String, Object>>> getCartRestaurants(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Map<String, Object>> restaurants = cartService.getCartRestaurants(userId);
        return ResponseEntity.ok(restaurants);
    }
    
    @PostMapping("/validate")
    @Operation(summary = "Validate cart", description = "Validate cart items availability and pricing")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> validateCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Map<String, Object> validation = cartService.validateCart(userId);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/restaurant/{restaurantId}/validate")
    @Operation(summary = "Validate restaurant cart", description = "Validate cart items for specific restaurant")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> validateRestaurantCart(@PathVariable Long restaurantId,
                                                                     Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Map<String, Object> validation = cartService.validateRestaurantCart(userId, restaurantId);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/save-for-later/{cartItemId}")
    @Operation(summary = "Save item for later", description = "Move cart item to saved for later list")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> saveForLater(@PathVariable Long cartItemId,
                                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            cartService.saveForLater(userId, cartItemId);
            return ResponseEntity.ok(Map.of("message", "Item saved for later"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/move-to-cart/{savedItemId}")
    @Operation(summary = "Move to cart", description = "Move saved item back to cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> moveToCart(@PathVariable Long savedItemId,
                                                        Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            cartService.moveToCart(userId, savedItemId);
            return ResponseEntity.ok(Map.of("message", "Item moved to cart"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/saved-items")
    @Operation(summary = "Get saved items", description = "Retrieve items saved for later")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CartItem>> getSavedItems(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<CartItem> savedItems = cartService.getSavedItems(userId);
        return ResponseEntity.ok(savedItems);
    }
    
    @PostMapping("/apply-coupon")
    @Operation(summary = "Apply coupon", description = "Apply discount coupon to cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> applyCoupon(@RequestParam String couponCode,
                                                          @RequestParam(required = false) Long restaurantId,
                                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Map<String, Object> result = cartService.applyCoupon(userId, couponCode, restaurantId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/remove-coupon")
    @Operation(summary = "Remove coupon", description = "Remove applied coupon from cart")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> removeCoupon(@RequestParam(required = false) Long restaurantId,
                                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            cartService.removeCoupon(userId, restaurantId);
            return ResponseEntity.ok(Map.of("message", "Coupon removed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Helper method
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Extract user ID from authentication principal
        // Implementation depends on your authentication setup
        return 1L; // Placeholder
    }
}