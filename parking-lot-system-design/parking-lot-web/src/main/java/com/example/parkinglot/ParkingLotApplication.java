package com.example.parkinglot;

import com.example.parkinglot.model.ParkingSpot;
import com.example.parkinglot.model.ParkingSpotType;
import com.example.parkinglot.model.SpotStatus;
import com.example.parkinglot.repository.ParkingSpotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.parkinglot.repository")
@EntityScan(basePackages = "com.example.parkinglot.model")
public class ParkingLotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingLotApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(ParkingSpotRepository repository) {
        return args -> {
            // Seed some spots
            ParkingSpot s1 = new ParkingSpot(null, "1A", ParkingSpotType.COMPACT, SpotStatus.FREE, 1);
            ParkingSpot s2 = new ParkingSpot(null, "1B", ParkingSpotType.COMPACT, SpotStatus.FREE, 1);
            ParkingSpot s3 = new ParkingSpot(null, "1C", ParkingSpotType.LARGE, SpotStatus.FREE, 1);
            ParkingSpot s4 = new ParkingSpot(null, "1D", ParkingSpotType.BIKE, SpotStatus.FREE, 1);

            repository.saveAll(Arrays.asList(s1, s2, s3, s4));
            System.out.println("Initialized parking spots.");
        };
    }
}
