package com.example.hotel;

import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import com.example.hotel.model.RoomType;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        if (hotelRepository.count() == 0) {
            System.out.println("Seeding Database...");

            Hotel grandHotel = Hotel.builder()
                    .name("Grand Budapest Hotel")
                    .address("Republic of Zubrowka")
                    .city("Zubrowka")
                    .build();
            
            hotelRepository.save(grandHotel);

            Room r101 = Room.builder().roomNumber("101").type(RoomType.SINGLE).pricePerNight(new BigDecimal("100.00")).hotel(grandHotel).build();
            Room r102 = Room.builder().roomNumber("102").type(RoomType.DOUBLE).pricePerNight(new BigDecimal("150.00")).hotel(grandHotel).build();
            Room r103 = Room.builder().roomNumber("103").type(RoomType.SUITE).pricePerNight(new BigDecimal("300.00")).hotel(grandHotel).build();

            roomRepository.saveAll(Arrays.asList(r101, r102, r103));

            System.out.println("Database Seeded with Hotel ID: " + grandHotel.getId());
        }
    }
}
