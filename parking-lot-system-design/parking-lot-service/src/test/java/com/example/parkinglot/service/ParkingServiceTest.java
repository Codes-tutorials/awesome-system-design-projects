package com.example.parkinglot.service;

import com.example.parkinglot.model.*;
import com.example.parkinglot.repository.ParkingSpotRepository;
import com.example.parkinglot.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private ParkingService parkingService;

    private ParkingSpot spot;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        spot = new ParkingSpot(1L, "1A", ParkingSpotType.COMPACT, SpotStatus.FREE, 1);
        ticket = new Ticket();
        ticket.setId("ticket-123");
        ticket.setVehicleType(VehicleType.CAR);
        ticket.setParkingSpot(spot);
        ticket.setEntryTime(LocalDateTime.now().minusHours(1));
        ticket.setStatus(TicketStatus.ACTIVE);
    }

    @Test
    void entry_Success() {
        when(parkingSpotRepository.findFirstByTypeAndStatus(ParkingSpotType.COMPACT, SpotStatus.FREE))
                .thenReturn(Optional.of(spot));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArguments()[0]);

        Ticket result = parkingService.entry("ABC-123", VehicleType.CAR, "GATE-1");

        assertNotNull(result);
        assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
        assertEquals("ABC-123", result.getVehicleNumber());
        verify(parkingSpotRepository).save(spot);
    }

    @Test
    void entry_Full() {
        when(parkingSpotRepository.findFirstByTypeAndStatus(ParkingSpotType.COMPACT, SpotStatus.FREE))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            parkingService.entry("ABC-123", VehicleType.CAR, "GATE-1")
        );
    }

    @Test
    void exit_Success() {
        when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(ticket));
        when(pricingService.calculateFee(any(Ticket.class))).thenReturn(10.0);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArguments()[0]);

        Ticket result = parkingService.exit("ticket-123", "GATE-2");

        assertEquals(TicketStatus.PAID, result.getStatus());
        assertEquals(10.0, result.getAmount());
        assertEquals(SpotStatus.FREE, spot.getStatus());
    }
}
