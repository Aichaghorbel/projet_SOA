package com.example.smartspace.service;

import com.example.smartspace.exception.ReservationOverlapException;
import com.example.smartspace.model.Reservation;
import com.example.smartspace.model.ReservationStatus;
import com.example.smartspace.model.Room;
import com.example.smartspace.repository.ReservationRepository;
import com.example.smartspace.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long roomId;

    @BeforeEach
    void setUp() {
        // Create a room for reservations
        Room room = new Room();
        room.setName("Salle A");
        room.setCapacity(10);
        room.setLocation("1st floor");
        Room saved = roomRepository.save(room);
        roomId = saved.getId();
    }

    @Test
    void createReservation_success() {
        Reservation reservation = new Reservation();
        reservation.setRoomId(roomId);
        reservation.setEtudiantId("student123");
        reservation.setStartTime(LocalDateTime.now().plusHours(1));
        reservation.setEndTime(LocalDateTime.now().plusHours(2));
        reservation.setExamenSessionId("session1");
        reservation.setStatus(ReservationStatus.PENDING);

        Reservation saved = reservationService.createReservation(reservation);
        assertNotNull(saved.getId());
        assertEquals(ReservationStatus.PENDING, saved.getStatus());
    }

    @Test
    void createReservation_overlapThrows() {
        // First reservation
        Reservation r1 = new Reservation();
        r1.setRoomId(roomId);
        r1.setEtudiantId("studentA");
        r1.setStartTime(LocalDateTime.now().plusHours(1));
        r1.setEndTime(LocalDateTime.now().plusHours(2));
        r1.setExamenSessionId("session1");
        r1.setStatus(ReservationStatus.CONFIRMED);
        reservationService.createReservation(r1);

        // Overlapping reservation
        Reservation r2 = new Reservation();
        r2.setRoomId(roomId);
        r2.setEtudiantId("studentB");
        r2.setStartTime(LocalDateTime.now().plusHours(1).plusMinutes(30)); // overlaps
        r2.setEndTime(LocalDateTime.now().plusHours(2).plusMinutes(30));
        r2.setExamenSessionId("session2");
        r2.setStatus(ReservationStatus.PENDING);

        assertThrows(ReservationOverlapException.class, () -> reservationService.createReservation(r2));
    }

    @Test
    void listReservations_returnsAll() {
        // Ensure list works after creating a reservation
        Reservation reservation = new Reservation();
        reservation.setRoomId(roomId);
        reservation.setEtudiantId("studentX");
        reservation.setStartTime(LocalDateTime.now().plusHours(3));
        reservation.setEndTime(LocalDateTime.now().plusHours(4));
        reservation.setExamenSessionId("sessionX");
        reservation.setStatus(ReservationStatus.PENDING);
        reservationService.createReservation(reservation);

        List<Reservation> all = reservationService.listReservations();
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(r -> r.getEtudiantId().equals("studentX")));
    }
}
