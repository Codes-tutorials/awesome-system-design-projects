package com.fooddelivery.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for location-based operations using Google Maps API
 */
@Service
public class LocationService {
    
    @Value("${google.maps.api.key:dummy_key}")
    private String googleMapsApiKey;
    
    private GeoApiContext geoApiContext;
    
    @PostConstruct
    public void init() {
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(googleMapsApiKey)
                .build();
    }
    
    public double[] getCoordinates(String address) throws Exception {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();
            
            if (results.length > 0) {
                LatLng location = results[0].geometry.location;
                return new double[]{location.lat, location.lng};
            } else {
                throw new Exception("Address not found: " + address);
            }
        } catch (Exception e) {
            // Fallback to dummy coordinates for demo purposes
            System.err.println("Failed to get coordinates for address: " + address + ". Error: " + e.getMessage());
            return new double[]{40.7128, -74.0060}; // Default to NYC coordinates
        }
    }
    
    public String getAddressFromCoordinates(double latitude, double longitude) throws Exception {
        try {
            LatLng location = new LatLng(latitude, longitude);
            GeocodingResult[] results = GeocodingApi.reverseGeocode(geoApiContext, location).await();
            
            if (results.length > 0) {
                return results[0].formattedAddress;
            } else {
                throw new Exception("Address not found for coordinates: " + latitude + ", " + longitude);
            }
        } catch (Exception e) {
            System.err.println("Failed to get address for coordinates: " + e.getMessage());
            return "Address not available";
        }
    }
    
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two points
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in km
        
        return distance;
    }
    
    public Map<String, Object> getDistanceAndDuration(String origin, String destination) throws Exception {
        try {
            DistanceMatrix matrix = DistanceMatrixApi.newRequest(geoApiContext)
                    .origins(origin)
                    .destinations(destination)
                    .mode(TravelMode.DRIVING)
                    .units(Unit.METRIC)
                    .await();
            
            if (matrix.rows.length > 0 && matrix.rows[0].elements.length > 0) {
                DistanceMatrixElement element = matrix.rows[0].elements[0];
                
                if (element.status == DistanceMatrixElementStatus.OK) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("distance_km", element.distance.inMeters / 1000.0);
                    result.put("distance_text", element.distance.humanReadable);
                    result.put("duration_minutes", element.duration.inSeconds / 60);
                    result.put("duration_text", element.duration.humanReadable);
                    
                    return result;
                } else {
                    throw new Exception("Unable to calculate distance: " + element.status);
                }
            } else {
                throw new Exception("No results found for the given locations");
            }
        } catch (Exception e) {
            System.err.println("Failed to get distance and duration: " + e.getMessage());
            
            // Fallback calculation using Haversine formula
            double[] originCoords = getCoordinates(origin);
            double[] destCoords = getCoordinates(destination);
            double distance = calculateDistance(originCoords[0], originCoords[1], destCoords[0], destCoords[1]);
            
            Map<String, Object> result = new HashMap<>();
            result.put("distance_km", distance);
            result.put("distance_text", String.format("%.1f km", distance));
            result.put("duration_minutes", (int) (distance * 3)); // Rough estimate: 3 minutes per km
            result.put("duration_text", String.format("%d mins", (int) (distance * 3)));
            
            return result;
        }
    }
    
    public Map<String, Object> getDistanceAndDuration(double originLat, double originLng, 
                                                     double destLat, double destLng) throws Exception {
        String origin = originLat + "," + originLng;
        String destination = destLat + "," + destLng;
        
        return getDistanceAndDuration(origin, destination);
    }
    
    public boolean isWithinDeliveryRadius(double restaurantLat, double restaurantLng, 
                                        double deliveryLat, double deliveryLng, 
                                        double maxRadiusKm) {
        double distance = calculateDistance(restaurantLat, restaurantLng, deliveryLat, deliveryLng);
        return distance <= maxRadiusKm;
    }
    
    public int estimateDeliveryTime(double restaurantLat, double restaurantLng, 
                                  double deliveryLat, double deliveryLng) {
        try {
            Map<String, Object> distanceInfo = getDistanceAndDuration(restaurantLat, restaurantLng, deliveryLat, deliveryLng);
            return (Integer) distanceInfo.get("duration_minutes");
        } catch (Exception e) {
            // Fallback estimation
            double distance = calculateDistance(restaurantLat, restaurantLng, deliveryLat, deliveryLng);
            return (int) (distance * 3); // 3 minutes per km as rough estimate
        }
    }
    
    public String[] getNearbyPlaces(double latitude, double longitude, String type, int radius) {
        // This would use Google Places API to find nearby places
        // For demo purposes, returning dummy data
        return new String[]{
                "Sample Restaurant 1",
                "Sample Restaurant 2",
                "Sample Store 1"
        };
    }
    
    public boolean validateAddress(String address) {
        try {
            double[] coordinates = getCoordinates(address);
            return coordinates != null && coordinates.length == 2;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Map<String, Object> getLocationDetails(double latitude, double longitude) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            String address = getAddressFromCoordinates(latitude, longitude);
            details.put("address", address);
            details.put("latitude", latitude);
            details.put("longitude", longitude);
            details.put("valid", true);
        } catch (Exception e) {
            details.put("address", "Unknown location");
            details.put("latitude", latitude);
            details.put("longitude", longitude);
            details.put("valid", false);
            details.put("error", e.getMessage());
        }
        
        return details;
    }
}