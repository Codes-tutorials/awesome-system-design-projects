package com.rideshare.repository;

import com.rideshare.model.Driver;
import com.rideshare.model.Vehicle;
import com.rideshare.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vehicle entity
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * Find vehicle by license plate
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    /**
     * Find vehicles by driver
     */
    List<Vehicle> findByDriver(Driver driver);
    
    /**
     * Find active vehicles by driver
     */
    @Query("SELECT v FROM Vehicle v WHERE v.driver = :driver AND v.isActive = true")
    List<Vehicle> findActiveVehiclesByDriver(@Param("driver") Driver driver);
    
    /**
     * Find primary vehicle for driver
     */
    @Query("SELECT v FROM Vehicle v WHERE v.driver = :driver AND v.isPrimary = true AND v.isActive = true")
    Optional<Vehicle> findPrimaryVehicleByDriver(@Param("driver") Driver driver);
    
    /**
     * Find vehicles by type
     */
    List<Vehicle> findByVehicleType(VehicleType vehicleType);
    
    /**
     * Find verified vehicles
     */
    @Query("SELECT v FROM Vehicle v WHERE v.isVerified = true AND v.isActive = true")
    List<Vehicle> findVerifiedVehicles();
    
    /**
     * Find vehicles needing verification
     */
    @Query("SELECT v FROM Vehicle v WHERE v.isVerified = false AND v.isActive = true")
    List<Vehicle> findVehiclesNeedingVerification();
    
    /**
     * Check if license plate exists
     */
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * Find vehicles by make and model
     */
    List<Vehicle> findByMakeAndModel(String make, String model);
    
    /**
     * Find vehicles by year range
     */
    @Query("SELECT v FROM Vehicle v WHERE v.year BETWEEN :startYear AND :endYear")
    List<Vehicle> findByYearRange(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);
    
    /**
     * Count vehicles by type
     */
    long countByVehicleType(VehicleType vehicleType);
    
    /**
     * Count active vehicles
     */
    long countByIsActiveTrue();
    
    /**
     * Find vehicles with expired documents
     */
    @Query("""
        SELECT v FROM Vehicle v 
        WHERE v.isActive = true 
        AND (v.registrationExpiryDate < CURRENT_DATE 
             OR v.insuranceExpiryDate < CURRENT_DATE)
        """)
    List<Vehicle> findVehiclesWithExpiredDocuments();
}