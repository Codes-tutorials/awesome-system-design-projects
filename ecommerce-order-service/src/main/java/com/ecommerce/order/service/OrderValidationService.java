package com.ecommerce.order.service;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.UpdateOrderRequest;
import com.ecommerce.order.exception.ValidationException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class OrderValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderValidationService.class);
    
    // Validation patterns
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]{8,36}$");
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]{8,36}$");
    private static final Pattern SKU_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]{3,50}$");
    private static final Pattern ADDRESS_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]{8,36}$");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    // Business rules constants
    private static final int MAX_ITEMS_PER_ORDER = 100;
    private static final int MAX_QUANTITY_PER_ITEM = 1000;
    private static final BigDecimal MAX_ITEM_PRICE = new BigDecimal("100000.00");
    private static final BigDecimal MAX_ORDER_TOTAL = new BigDecimal("1000000.00");
    private static final BigDecimal MIN_ORDER_TOTAL = new BigDecimal("0.01");
    private static final int MAX_NOTES_LENGTH = 1000;
    private static final int MAX_GIFT_MESSAGE_LENGTH = 500;
    private static final int MAX_SPECIAL_INSTRUCTIONS_LENGTH = 500;
    
    /**
     * Validate create order request
     */
    public void validateCreateOrderRequest(CreateOrderRequest request) {
        log.debug("Validating create order request for user: {}", request.getUserId());
        
        List<String> errors = new ArrayList<>();
        
        // Validate user ID
        validateUserId(request.getUserId(), errors);
        
        // Validate currency
        validateCurrency(request.getCurrency(), errors);
        
        // Validate addresses
        validateAddressId(request.getShippingAddressId(), "shipping", errors);
        if (request.getBillingAddressId() != null) {
            validateAddressId(request.getBillingAddressId(), "billing", errors);
        }
        
        // Validate amounts
        validateAmounts(request, errors);
        
        // Validate order items
        validateOrderItems(request.getItems(), errors);
        
        // Validate notes
        validateNotes(request.getNotes(), errors);
        
        // Validate flash sale specific fields
        if (Boolean.TRUE.equals(request.getIsFlashSale())) {
            validateFlashSaleFields(request, errors);
        }
        
        // Business rule validations
        validateBusinessRules(request, errors);
        
        if (!errors.isEmpty()) {
            String errorMessage = "Validation failed: " + String.join(", ", errors);
            log.warn("Order validation failed for user {}: {}", request.getUserId(), errorMessage);
            throw new ValidationException(errorMessage);
        }
        
        log.debug("Order validation passed for user: {}", request.getUserId());
    }
    
    /**
     * Validate update order request
     */
    public void validateUpdateOrderRequest(Order existingOrder, UpdateOrderRequest request) {
        log.debug("Validating update order request for order: {}", existingOrder.getOrderId());
        
        List<String> errors = new ArrayList<>();
        
        // Check if order can be modified
        if (!existingOrder.isModifiable()) {
            errors.add("Order cannot be modified in current status: " + existingOrder.getStatus());
        }
        
        // Validate addresses if provided
        if (request.getShippingAddressId() != null) {
            validateAddressId(request.getShippingAddressId(), "shipping", errors);
        }
        
        if (request.getBillingAddressId() != null) {
            validateAddressId(request.getBillingAddressId(), "billing", errors);
        }
        
        // Validate notes
        validateNotes(request.getNotes(), errors);
        
        if (!errors.isEmpty()) {
            String errorMessage = "Update validation failed: " + String.join(", ", errors);
            log.warn("Order update validation failed for order {}: {}", existingOrder.getOrderId(), errorMessage);
            throw new ValidationException(errorMessage);
        }
        
        log.debug("Order update validation passed for order: {}", existingOrder.getOrderId());
    }
    
    /**
     * Validate user ID format
     */
    private void validateUserId(String userId, List<String> errors) {
        if (userId == null || userId.trim().isEmpty()) {
            errors.add("User ID is required");
            return;
        }
        
        if (!USER_ID_PATTERN.matcher(userId).matches()) {
            errors.add("Invalid user ID format");
        }
    }
    
    /**
     * Validate currency code
     */
    private void validateCurrency(String currency, List<String> errors) {
        if (currency == null || currency.trim().isEmpty()) {
            errors.add("Currency is required");
            return;
        }
        
        if (!CURRENCY_PATTERN.matcher(currency).matches()) {
            errors.add("Invalid currency format (must be 3-letter ISO code)");
        }
        
        // Check supported currencies
        List<String> supportedCurrencies = List.of("USD", "EUR", "GBP", "JPY", "CAD", "AUD");
        if (!supportedCurrencies.contains(currency)) {
            errors.add("Unsupported currency: " + currency);
        }
    }
    
    /**
     * Validate address ID
     */
    private void validateAddressId(String addressId, String addressType, List<String> errors) {
        if (addressId == null || addressId.trim().isEmpty()) {
            errors.add(addressType + " address ID is required");
            return;
        }
        
        if (!ADDRESS_ID_PATTERN.matcher(addressId).matches()) {
            errors.add("Invalid " + addressType + " address ID format");
        }
    }
    
    /**
     * Validate monetary amounts
     */
    private void validateAmounts(CreateOrderRequest request, List<String> errors) {
        // Validate individual amounts
        if (request.getDiscountAmount() != null && request.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Discount amount cannot be negative");
        }
        
        if (request.getTaxAmount() != null && request.getTaxAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Tax amount cannot be negative");
        }
        
        if (request.getShippingAmount() != null && request.getShippingAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Shipping amount cannot be negative");
        }
        
        // Validate total amount
        BigDecimal totalAmount = request.calculateTotalAmount();
        if (totalAmount.compareTo(MIN_ORDER_TOTAL) < 0) {
            errors.add("Order total must be at least " + MIN_ORDER_TOTAL);
        }
        
        if (totalAmount.compareTo(MAX_ORDER_TOTAL) > 0) {
            errors.add("Order total cannot exceed " + MAX_ORDER_TOTAL);
        }
        
        // Validate discount doesn't exceed subtotal
        BigDecimal subtotal = request.calculateSubtotal();
        if (request.getDiscountAmount() != null && request.getDiscountAmount().compareTo(subtotal) > 0) {
            errors.add("Discount amount cannot exceed subtotal");
        }
    }
    
    /**
     * Validate order items
     */
    private void validateOrderItems(List<CreateOrderRequest.OrderItemRequest> items, List<String> errors) {
        if (items == null || items.isEmpty()) {
            errors.add("Order must contain at least one item");
            return;
        }
        
        if (items.size() > MAX_ITEMS_PER_ORDER) {
            errors.add("Order cannot contain more than " + MAX_ITEMS_PER_ORDER + " items");
        }
        
        for (int i = 0; i < items.size(); i++) {
            CreateOrderRequest.OrderItemRequest item = items.get(i);
            validateOrderItem(item, i + 1, errors);
        }
    }
    
    /**
     * Validate individual order item
     */
    private void validateOrderItem(CreateOrderRequest.OrderItemRequest item, int itemNumber, List<String> errors) {
        String itemPrefix = "Item " + itemNumber + ": ";
        
        // Validate product ID
        if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
            errors.add(itemPrefix + "Product ID is required");
        } else if (!PRODUCT_ID_PATTERN.matcher(item.getProductId()).matches()) {
            errors.add(itemPrefix + "Invalid product ID format");
        }
        
        // Validate SKU
        if (item.getSku() == null || item.getSku().trim().isEmpty()) {
            errors.add(itemPrefix + "SKU is required");
        } else if (!SKU_PATTERN.matcher(item.getSku()).matches()) {
            errors.add(itemPrefix + "Invalid SKU format");
        }
        
        // Validate quantity
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            errors.add(itemPrefix + "Quantity must be positive");
        } else if (item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
            errors.add(itemPrefix + "Quantity cannot exceed " + MAX_QUANTITY_PER_ITEM);
        }
        
        // Validate unit price
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add(itemPrefix + "Unit price must be non-negative");
        } else if (item.getUnitPrice().compareTo(MAX_ITEM_PRICE) > 0) {
            errors.add(itemPrefix + "Unit price cannot exceed " + MAX_ITEM_PRICE);
        }
        
        // Validate calculated total price
        if (item.getQuantity() != null && item.getUnitPrice() != null) {
            BigDecimal expectedTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            if (item.getTotalPrice() != null && item.getTotalPrice().compareTo(expectedTotal) != 0) {
                errors.add(itemPrefix + "Total price doesn't match unit price × quantity");
            }
        }
        
        // Validate optional fields
        if (item.getGiftMessage() != null && item.getGiftMessage().length() > MAX_GIFT_MESSAGE_LENGTH) {
            errors.add(itemPrefix + "Gift message too long (max " + MAX_GIFT_MESSAGE_LENGTH + " characters)");
        }
        
        if (item.getSpecialInstructions() != null && item.getSpecialInstructions().length() > MAX_SPECIAL_INSTRUCTIONS_LENGTH) {
            errors.add(itemPrefix + "Special instructions too long (max " + MAX_SPECIAL_INSTRUCTIONS_LENGTH + " characters)");
        }
        
        // Validate weight if provided
        if (item.getWeight() != null && item.getWeight().compareTo(BigDecimal.ZERO) < 0) {
            errors.add(itemPrefix + "Weight cannot be negative");
        }
    }
    
    /**
     * Validate notes field
     */
    private void validateNotes(String notes, List<String> errors) {
        if (notes != null && notes.length() > MAX_NOTES_LENGTH) {
            errors.add("Notes too long (max " + MAX_NOTES_LENGTH + " characters)");
        }
        
        // Check for potentially malicious content
        if (notes != null && containsSuspiciousContent(notes)) {
            errors.add("Notes contain invalid content");
        }
    }
    
    /**
     * Validate flash sale specific fields
     */
    private void validateFlashSaleFields(CreateOrderRequest request, List<String> errors) {
        if (request.getFlashSaleId() == null || request.getFlashSaleId().trim().isEmpty()) {
            errors.add("Flash sale ID is required for flash sale orders");
        }
        
        // Flash sale orders should have limited items
        if (request.getItems() != null && request.getItems().size() > 10) {
            errors.add("Flash sale orders cannot contain more than 10 items");
        }
    }
    
    /**
     * Validate business rules
     */
    private void validateBusinessRules(CreateOrderRequest request, List<String> errors) {
        // Check for duplicate SKUs
        if (request.getItems() != null) {
            List<String> skus = request.getItems().stream()
                    .map(CreateOrderRequest.OrderItemRequest::getSku)
                    .toList();
            
            if (skus.size() != skus.stream().distinct().count()) {
                errors.add("Duplicate SKUs are not allowed in the same order");
            }
        }
        
        // Validate payment method if provided
        if (request.getPaymentMethod() != null) {
            List<String> validPaymentMethods = List.of("CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "APPLE_PAY", "GOOGLE_PAY");
            if (!validPaymentMethods.contains(request.getPaymentMethod())) {
                errors.add("Invalid payment method: " + request.getPaymentMethod());
            }
        }
        
        // Validate source
        if (request.getSource() != null) {
            List<String> validSources = List.of("WEB", "MOBILE", "API", "ADMIN");
            if (!validSources.contains(request.getSource())) {
                errors.add("Invalid order source: " + request.getSource());
            }
        }
    }
    
    /**
     * Check for suspicious content in text fields
     */
    private boolean containsSuspiciousContent(String text) {
        if (text == null) return false;
        
        String lowerText = text.toLowerCase();
        
        // Check for SQL injection patterns
        String[] sqlPatterns = {"select ", "insert ", "update ", "delete ", "drop ", "union ", "script", "<script"};
        for (String pattern : sqlPatterns) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        
        // Check for XSS patterns
        String[] xssPatterns = {"<script", "javascript:", "onload=", "onerror=", "onclick="};
        for (String pattern : xssPatterns) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate order status transition
     */
    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        log.debug("Validating status transition from {} to {}", currentStatus, newStatus);
        
        boolean validTransition = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || 
                          newStatus == OrderStatus.CANCELLED || 
                          newStatus == OrderStatus.PAYMENT_FAILED;
            
            case CONFIRMED -> newStatus == OrderStatus.PROCESSING || 
                            newStatus == OrderStatus.CANCELLED;
            
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || 
                             newStatus == OrderStatus.CANCELLED;
            
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || 
                          newStatus == OrderStatus.RETURNED;
            
            case DELIVERED -> newStatus == OrderStatus.RETURNED || 
                            newStatus == OrderStatus.REFUNDED;
            
            case CANCELLED, PAYMENT_FAILED, RETURNED, REFUNDED -> false; // Terminal states
        };
        
        if (!validTransition) {
            throw new ValidationException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
        
        log.debug("Status transition validation passed");
    }
    
    /**
     * Validate order cancellation
     */
    public void validateCancellation(Order order, String reason) {
        log.debug("Validating cancellation for order: {}", order.getOrderId());
        
        if (!order.isCancellable()) {
            throw new ValidationException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Cancellation reason is required");
        }
        
        if (reason.length() > 500) {
            throw new ValidationException("Cancellation reason too long (max 500 characters)");
        }
        
        log.debug("Cancellation validation passed for order: {}", order.getOrderId());
    }
}