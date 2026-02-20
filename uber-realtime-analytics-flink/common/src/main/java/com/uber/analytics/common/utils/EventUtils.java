package com.uber.analytics.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Utility class for event processing operations
 */
public class EventUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(EventUtils.class);
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    /**
     * Generate a unique event ID
     */
    public static String generateEventId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Get current timestamp in milliseconds
     */
    public static long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }
    
    /**
     * Serialize Avro record to byte array
     */
    public static <T extends SpecificRecord> byte[] serializeAvro(T record) throws IOException {
        DatumWriter<T> datumWriter = new SpecificDatumWriter<>(record.getSchema());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        
        datumWriter.write(record, encoder);
        encoder.flush();
        
        return outputStream.toByteArray();
    }
    
    /**
     * Deserialize byte array to Avro record
     */
    public static <T extends SpecificRecord> T deserializeAvro(byte[] data, Class<T> clazz) throws IOException {
        try {
            T record = clazz.getDeclaredConstructor().newInstance();
            DatumReader<T> datumReader = new SpecificDatumReader<>(record.getSchema());
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            
            return datumReader.read(null, decoder);
        } catch (Exception e) {
            throw new IOException("Failed to deserialize Avro record", e);
        }
    }
    
    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("Failed to convert object to JSON", e);
            return "{}";
        }
    }
    
    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Failed to convert JSON to object", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Calculate estimated time of arrival based on distance and average speed
     */
    public static long calculateETA(double distanceKm, double averageSpeedKmh) {
        if (averageSpeedKmh <= 0) {
            averageSpeedKmh = 30; // Default speed
        }
        
        double timeHours = distanceKm / averageSpeedKmh;
        return Math.round(timeHours * 60 * 60 * 1000); // Convert to milliseconds
    }
    
    /**
     * Validate latitude value
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }
    
    /**
     * Validate longitude value
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }
    
    /**
     * Generate a geohash for location-based partitioning
     */
    public static String generateGeohash(double latitude, double longitude, int precision) {
        // Simple geohash implementation for demonstration
        // In production, use a proper geohash library
        String latBinary = Long.toBinaryString(Double.doubleToLongBits(latitude));
        String lonBinary = Long.toBinaryString(Double.doubleToLongBits(longitude));
        
        StringBuilder geohash = new StringBuilder();
        int minLength = Math.min(latBinary.length(), lonBinary.length());
        
        for (int i = 0; i < Math.min(precision, minLength); i++) {
            geohash.append(lonBinary.charAt(i));
            geohash.append(latBinary.charAt(i));
        }
        
        return geohash.toString();
    }
    
    /**
     * Extract city from coordinates (simplified implementation)
     */
    public static String extractCity(double latitude, double longitude) {
        // This is a simplified implementation
        // In production, use a proper geocoding service
        if (latitude >= 37.7 && latitude <= 37.8 && longitude >= -122.5 && longitude <= -122.4) {
            return "San Francisco";
        } else if (latitude >= 40.7 && latitude <= 40.8 && longitude >= -74.1 && longitude <= -73.9) {
            return "New York";
        } else if (latitude >= 34.0 && latitude <= 34.1 && longitude >= -118.3 && longitude <= -118.2) {
            return "Los Angeles";
        }
        return "Unknown";
    }
    
    /**
     * Check if timestamp is within acceptable time window (for late event handling)
     */
    public static boolean isWithinTimeWindow(long eventTimestamp, long currentTimestamp, long windowSizeMs) {
        return Math.abs(currentTimestamp - eventTimestamp) <= windowSizeMs;
    }
}