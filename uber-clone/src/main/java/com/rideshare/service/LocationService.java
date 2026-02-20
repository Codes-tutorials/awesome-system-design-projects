package com.rideshare.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import com.rideshare.model.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling location-based operations
 * Integrates with Google Maps API for geocoding, routing, and distance calculations
 */
@Service
public class LocationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;
    
    private final RedisTemplate<String, Object> redisTemplate;
    private GeoApiContext geoApiContext;
    
    public LocationService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Initialize Google Maps API context
     */
    private GeoApiContext getGeoApiContext() {
        if (geoApiContext == null) {
            geoApiContext = new GeoApiContext.Builder()
                .apiKey(googleMapsApiKey)
                .build();
        }
        return geoApiContext;
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    public double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return Double.MAX_VALUE;
        }
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Get route information between two points
     */
    public RouteInfo getRouteInfo(Double originLat, Double originLng, 
                                 Double destLat, Double destLng) {
        try {
            String cacheKey = String.format("route:%f,%f:%f,%f", originLat, originLng, destLat, destLng);
            
            // Check cache first
            RouteInfo cachedRoute = (RouteInfo) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRoute != null) {
                return cachedRoute;
            }
            
            // Call Google Directions API
            DirectionsResult result = DirectionsApi.newRequest(getGeoApiContext())
                .origin(new LatLng(originLat, originLng))
                .destination(new LatLng(destLat, destLng))
                .mode(TravelMode.DRIVING)
                .optimizeWaypoints(true)
                .await();
            
            if (result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                DirectionsLeg leg = route.legs[0];
                
                RouteInfo routeInfo = new RouteInfo(
                    leg.distance.inMeters / 1000.0, // Convert to kilometers
                    leg.duration.inSeconds / 60, // Convert to minutes
                    route.overviewPolyline.getEncodedPath()
                );
                
                // Cache for 10 minutes
                redisTemplate.opsForValue().set(cacheKey, routeInfo, 10, TimeUnit.MINUTES);
                
                return routeInfo;
            }
            
        } catch (Exception e) {
            logger.error("Error getting route info from Google Maps API", e);
        }
        
        // Fallback to straight-line distance
        double distance = calculateDistance(originLat, originLng, destLat, destLng);
        int estimatedMinutes = (int) (distance * 2); // Rough estimate: 2 minutes per km
        
        return new RouteInfo(distance, estimatedMinutes, null);
    }
    
    /**
     * Geocode an address to get coordinates
     */
    public LocationCoordinates geocodeAddress(String address) {
        try {
            String cacheKey = "geocode:" + address.toLowerCase().trim();
            
            // Check cache first
            LocationCoordinates cached = (LocationCoordinates) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
            
            // Call Google Geocoding API
            GeocodingResult[] results = GeocodingApi.geocode(getGeoApiContext(), address).await();
            
            if (results.length > 0) {
                Geometry geometry = results[0].geometry;
                LocationCoordinates coordinates = new LocationCoordinates(
                    geometry.location.lat,
                    geometry.location.lng,
                    results[0].formattedAddress
                );
                
                // Cache for 24 hours
                redisTemplate.opsForValue().set(cacheKey, coordinates, 24, TimeUnit.HOURS);
                
                return coordinates;
            }
            
        } catch (Exception e) {
            logger.error("Error geocoding address: {}", address, e);
        }
        
        return null;
    }
    
    /**
     * Reverse geocode coordinates to get address
     */
    public String reverseGeocode(Double latitude, Double longitude) {
        try {
            String cacheKey = String.format("reverse_geocode:%f,%f", latitude, longitude);
            
            // Check cache first
            String cached = (String) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
            
            // Call Google Geocoding API
            GeocodingResult[] results = GeocodingApi.reverseGeocode(
                getGeoApiContext(), 
                new LatLng(latitude, longitude)
            ).await();
            
            if (results.length > 0) {
                String address = results[0].formattedAddress;
                
                // Cache for 24 hours
                redisTemplate.opsForValue().set(cacheKey, address, 24, TimeUnit.HOURS);
                
                return address;
            }
            
        } catch (Exception e) {
            logger.error("Error reverse geocoding coordinates: {}, {}", latitude, longitude, e);
        }
        
        return "Unknown Location";
    }
    
    /**
     * Update driver location in real-time
     */
    public void updateDriverLocation(Long driverId, Double latitude, Double longitude, Double heading) {
        try {
            // Store in Redis for real-time tracking
            String locationKey = "driver_location:" + driverId;
            DriverLocation location = new DriverLocation(
                driverId, latitude, longitude, heading, LocalDateTime.now()
            );
            
            redisTemplate.opsForValue().set(locationKey, location, 5, TimeUnit.MINUTES);
            
            // Also store in geospatial index for proximity queries
            String geoKey = "driver_geo";
            redisTemplate.opsForGeo().add(geoKey, new org.springframework.data.geo.Point(longitude, latitude), driverId.toString());
            
            logger.debug("Updated location for driver {}: {}, {}", driverId, latitude, longitude);
            
        } catch (Exception e) {
            logger.error("Error updating driver location for driver {}", driverId, e);
        }
    }
    
    /**
     * Get driver's current location from cache
     */
    public DriverLocation getDriverLocation(Long driverId) {
        try {
            String locationKey = "driver_location:" + driverId;
            return (DriverLocation) redisTemplate.opsForValue().get(locationKey);
        } catch (Exception e) {
            logger.error("Error getting driver location for driver {}", driverId, e);
            return null;
        }
    }
    
    /**
     * Find nearby drivers using Redis geospatial queries
     */
    public java.util.List<Long> findNearbyDrivers(Double latitude, Double longitude, double radiusKm) {
        try {
            String geoKey = "driver_geo";
            
            // Use Redis GEORADIUS command
            org.springframework.data.geo.Circle circle = new org.springframework.data.geo.Circle(
                new org.springframework.data.geo.Point(longitude, latitude),
                new org.springframework.data.geo.Distance(radiusKm, org.springframework.data.geo.Metrics.KILOMETERS)
            );
            
            return redisTemplate.opsForGeo()
                .radius(geoKey, circle)
                .getContent()
                .stream()
                .map(result -> Long.parseLong(result.getContent().getName()))
                .toList();
                
        } catch (Exception e) {
            logger.error("Error finding nearby drivers", e);
            return java.util.List.of();
        }
    }
    
    /**
     * Calculate ETA from driver to pickup location
     */
    public int calculateETA(Double driverLat, Double driverLng, 
                           Double pickupLat, Double pickupLng) {
        RouteInfo route = getRouteInfo(driverLat, driverLng, pickupLat, pickupLng);
        return route.getDurationMinutes();
    }
    
    /**
     * Validate coordinates
     */
    public boolean isValidCoordinates(Double latitude, Double longitude) {
        return latitude != null && longitude != null &&
               latitude >= -90 && latitude <= 90 &&
               longitude >= -180 && longitude <= 180;
    }
    
    /**
     * Check if location is within service area
     */
    public boolean isWithinServiceArea(Double latitude, Double longitude) {
        // This would check against predefined service boundaries
        // For now, we'll just validate coordinates
        return isValidCoordinates(latitude, longitude);
    }
    
    // Inner classes for data transfer
    public static class RouteInfo {
        private double distanceKm;
        private int durationMinutes;
        private String encodedPolyline;
        
        public RouteInfo(double distanceKm, int durationMinutes, String encodedPolyline) {
            this.distanceKm = distanceKm;
            this.durationMinutes = durationMinutes;
            this.encodedPolyline = encodedPolyline;
        }
        
        // Getters and setters
        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
        
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        
        public String getEncodedPolyline() { return encodedPolyline; }
        public void setEncodedPolyline(String encodedPolyline) { this.encodedPolyline = encodedPolyline; }
    }
    
    public static class LocationCoordinates {
        private double latitude;
        private double longitude;
        private String formattedAddress;
        
        public LocationCoordinates(double latitude, double longitude, String formattedAddress) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.formattedAddress = formattedAddress;
        }
        
        // Getters and setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
    }
    
    public static class DriverLocation {
        private Long driverId;
        private Double latitude;
        private Double longitude;
        private Double heading;
        private LocalDateTime timestamp;
        
        public DriverLocation(Long driverId, Double latitude, Double longitude, 
                            Double heading, LocalDateTime timestamp) {
            this.driverId = driverId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.heading = heading;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }
        
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public Double getHeading() { return heading; }
        public void setHeading(Double heading) { this.heading = heading; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}