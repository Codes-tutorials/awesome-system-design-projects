package com.uber.analytics.gateway.controller;

import com.uber.analytics.gateway.dto.RideRequestDto;
import com.uber.analytics.gateway.dto.RideResponseDto;
import com.uber.analytics.gateway.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for ride operations
 */
@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = "*")
public class RideController {
    
    @Autowired
    private RideService rideService;
    
    @PostMapping("/request")
    public ResponseEntity<RideResponseDto> requestRide(@Valid @RequestBody RideRequestDto request) {
        RideResponseDto response = rideService.requestRide(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponseDto> getRide(@PathVariable String rideId) {
        RideResponseDto ride = rideService.getRide(rideId);
        return ResponseEntity.ok(ride);
    }
    
    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<RideResponseDto>> getRidesByRider(@PathVariable String riderId) {
        List<RideResponseDto> rides = rideService.getRidesByRider(riderId);
        return ResponseEntity.ok(rides);
    }
    
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponseDto> cancelRide(@PathVariable String rideId) {
        RideResponseDto response = rideService.cancelRide(rideId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{rideId}/complete")
    public ResponseEntity<RideResponseDto> completeRide(@PathVariable String rideId) {
        RideResponseDto response = rideService.completeRide(rideId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/estimate")
    public ResponseEntity<Double> estimateFare(
            @RequestParam double pickupLat,
            @RequestParam double pickupLng,
            @RequestParam double dropoffLat,
            @RequestParam double dropoffLng,
            @RequestParam String rideType) {
        
        double estimate = rideService.estimateFare(pickupLat, pickupLng, dropoffLat, dropoffLng, rideType);
        return ResponseEntity.ok(estimate);
    }
}