package com.fooddelivery.repository;

import com.fooddelivery.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Address entity operations
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserId(Long userId);
    
    List<Address> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Address findDefaultAddressByUserId(@Param("userId") Long userId);
    
    List<Address> findByCity(String city);
    
    List<Address> findByState(String state);
    
    List<Address> findByPostalCode(String postalCode);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.addressType = :addressType")
    List<Address> findByUserIdAndAddressType(@Param("userId") Long userId, @Param("addressType") String addressType);
    
    @Query("SELECT DISTINCT a.city FROM Address a WHERE a.isActive = true ORDER BY a.city")
    List<String> findAllActiveCities();
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId AND a.isActive = true")
    Long countActiveAddressesByUser(@Param("userId") Long userId);
}