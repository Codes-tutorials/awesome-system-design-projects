package com.fooddelivery.service;

import com.fooddelivery.model.*;
import com.fooddelivery.repository.DeliveryPartnerRepository;
import com.fooddelivery.repository.DeliveryEarningRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for DeliveryPartner entity operations
 */
@Service
@Transactional
public class DeliveryPartnerService {
    
    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;
    
    @Autowired
    private DeliveryEarningRepository deliveryEarningRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private NotificationService notificationService;
    
    public DeliveryPartner createDeliveryPartner(Long userId, VehicleType vehicleType, String vehicleNumber,
                                               String licenseNumber, LocalDateTime licenseExpiryDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (!user.isDeliveryPartner()) {
            throw new RuntimeException("User is not a delivery partner");
        }
        
        // Check if user already has a delivery partner profile
        if (deliveryPartnerRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("User already has a delivery partner profile");
        }
        
        DeliveryPartner deliveryPartner = new DeliveryPartner(user, vehicleType, vehicleNumber);
        deliveryPartner.setLicenseNumber(licenseNumber);
        deliveryPartner.setLicenseExpiryDate(licenseExpiryDate);
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public Optional<DeliveryPartner> findById(Long id) {
        return deliveryPartnerRepository.findById(id);
    }
    
    public Optional<DeliveryPartner> findByUserId(Long userId) {
        return deliveryPartnerRepository.findByUserId(userId);
    }
    
    public Optional<DeliveryPartner> findByPartnerId(String partnerId) {
        return deliveryPartnerRepository.findByPartnerId(partnerId);
    }
    
    public DeliveryPartner updateProfile(Long deliveryPartnerId, VehicleType vehicleType, String vehicleNumber,
                                       String licenseNumber, LocalDateTime licenseExpiryDate) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.setVehicleType(vehicleType);
        deliveryPartner.setVehicleNumber(vehicleNumber);
        deliveryPartner.setLicenseNumber(licenseNumber);
        deliveryPartner.setLicenseExpiryDate(licenseExpiryDate);
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner updateBankDetails(Long deliveryPartnerId, String accountNumber, String ifscCode, String accountHolderName) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.setBankAccountNumber(accountNumber);
        deliveryPartner.setBankIfscCode(ifscCode);
        deliveryPartner.setBankAccountHolderName(accountHolderName);
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner updateEmergencyContact(Long deliveryPartnerId, String contactName, String contactPhone) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.setEmergencyContactName(contactName);
        deliveryPartner.setEmergencyContactPhone(contactPhone);
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner updateWorkingHours(Long deliveryPartnerId, Integer startHour, Integer endHour) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.setWorkingHoursStart(startHour);
        deliveryPartner.setWorkingHoursEnd(endHour);
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner updateLocation(Long deliveryPartnerId, Double latitude, Double longitude) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.updateLocation(latitude, longitude);
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner goOnline(Long deliveryPartnerId) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        if (!deliveryPartner.getIsVerified()) {
            throw new RuntimeException("Delivery partner is not verified");
        }
        
        if (!deliveryPartner.getIsActive()) {
            throw new RuntimeException("Delivery partner account is not active");
        }
        
        deliveryPartner.setIsOnline(true);
        deliveryPartner.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        deliveryPartner.setLastActiveAt(LocalDateTime.now());
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner goOffline(Long deliveryPartnerId) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.setIsOnline(false);
        deliveryPartner.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner updateAvailabilityStatus(Long deliveryPartnerId, AvailabilityStatus status) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.setAvailabilityStatus(status);
        if (status == AvailabilityStatus.OFFLINE) {
            deliveryPartner.setIsOnline(false);
        }
        
        return deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner assignDeliveryPartner(Order order) {
        // Find nearby available delivery partners
        List<DeliveryPartner> availablePartners = deliveryPartnerRepository.findNearbyAvailablePartners(
                order.getRestaurant().getLatitude(),
                order.getRestaurant().getLongitude(),
                10.0 // 10km radius
        );
        
        if (availablePartners.isEmpty()) {
            throw new RuntimeException("No available delivery partners found");
        }
        
        // Select the best partner (closest with good rating)
        DeliveryPartner selectedPartner = availablePartners.get(0);
        
        // Update partner status
        selectedPartner.setAvailabilityStatus(AvailabilityStatus.BUSY);
        deliveryPartnerRepository.save(selectedPartner);
        
        return selectedPartner;
    }
    
    public void completeDelivery(Long deliveryPartnerId, Order order) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        // Update delivery stats
        deliveryPartner.completeDelivery(true);
        
        // Calculate and record earnings
        BigDecimal deliveryFee = order.getDeliveryFee();
        BigDecimal commission = deliveryFee.multiply(deliveryPartner.getCommissionRate().divide(new BigDecimal("100")));
        BigDecimal partnerEarning = deliveryFee.subtract(commission);
        
        deliveryPartner.addEarning(partnerEarning);
        
        // Create earning record
        DeliveryEarning earning = new DeliveryEarning();
        earning.setDeliveryPartner(deliveryPartner);
        earning.setOrder(order);
        earning.setDeliveryFee(deliveryFee);
        earning.setCommissionRate(deliveryPartner.getCommissionRate());
        earning.setCommissionAmount(commission);
        earning.setPartnerEarning(partnerEarning);
        earning.setTotalEarning(partnerEarning);
        deliveryEarningRepository.save(earning);
        
        // Update availability status
        deliveryPartner.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        
        deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public DeliveryPartner verifyDeliveryPartner(Long deliveryPartnerId) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.setIsVerified(true);
        deliveryPartner.setOnboardingCompletedAt(LocalDateTime.now());
        
        DeliveryPartner savedPartner = deliveryPartnerRepository.save(deliveryPartner);
        
        // Send verification notification
        notificationService.sendDeliveryPartnerVerification(savedPartner);
        
        return savedPartner;
    }
    
    public List<DeliveryPartner> findAvailablePartners() {
        return deliveryPartnerRepository.findAvailablePartners();
    }
    
    public List<DeliveryPartner> findNearbyAvailablePartners(Double latitude, Double longitude, Double radiusKm) {
        return deliveryPartnerRepository.findNearbyAvailablePartners(latitude, longitude, radiusKm);
    }
    
    public List<DeliveryPartner> findTopRatedPartners(BigDecimal minRating) {
        return deliveryPartnerRepository.findTopRatedPartners(minRating);
    }
    
    public List<DeliveryPartner> findPartnersWithExpiringLicenses(int days) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
        return deliveryPartnerRepository.findPartnersWithExpiringLicenses(expiryDate);
    }
    
    public void updatePartnerRating(Long deliveryPartnerId, BigDecimal rating) {
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(deliveryPartnerId)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found with id: " + deliveryPartnerId));
        
        deliveryPartner.updateRating(rating);
        deliveryPartnerRepository.save(deliveryPartner);
    }
    
    public Long countActivePartners() {
        return deliveryPartnerRepository.countActivePartners();
    }
    
    public Long countOnlinePartners() {
        return deliveryPartnerRepository.countOnlinePartners();
    }
    
    public Long countAvailablePartners() {
        return deliveryPartnerRepository.countAvailablePartners();
    }
    
    public List<Object[]> getPartnerStatsByVehicleType() {
        return deliveryPartnerRepository.countPartnersByVehicleType();
    }
    
    public BigDecimal getAveragePartnerRating() {
        return deliveryPartnerRepository.getAveragePartnerRating();
    }
}