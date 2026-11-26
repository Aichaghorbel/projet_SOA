package com.educampus.smartspace.service;

import com.educampus.smartspace.exception.ReservationOverlapException;
import com.educampus.smartspace.model.Reservation;
import com.educampus.smartspace.model.ReservationStatus;
import com.educampus.smartspace.model.Room;
import com.educampus.smartspace.repository.ReservationRepository;
import com.educampus.smartspace.repository.RoomRepository;
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

    @Test
    void getReservation_existing() {
        Reservation reservation = new Reservation();
        reservation.setRoomId(roomId);
        reservation.setEtudiantId("studentGet");
        reservation.setStartTime(LocalDateTime.now().plusHours(5));
        reservation.setEndTime(LocalDateTime.now().plusHours(6));
        reservation.setExamenSessionId("sessGet");
        reservation.setStatus(ReservationStatus.PENDING);
        Reservation saved = reservationService.createReservation(reservation);

        assertTrue(reservationService.getReservation(saved.getId()).isPresent());
    }

    @Test
    void updateReservation_success() {
        Reservation reservation = new Reservation();
        reservation.setRoomId(roomId);
        reservation.setEtudiantId("studentUpd");
        reservation.setStartTime(LocalDateTime.now().plusHours(7));
        reservation.setEndTime(LocalDateTime.now().plusHours(8));
        reservation.setExamenSessionId("sessUpd");
        reservation.setStatus(ReservationStatus.PENDING);
        Reservation saved = reservationService.createReservation(reservation);

        Reservation updateData = new Reservation();
        updateData.setStartTime(LocalDateTime.now().plusHours(7));
        updateData.setEndTime(LocalDateTime.now().plusHours(9)); // extend 1 hour
        updateData.setEtudiantId("studentUpd");
        updateData.setExamenSessionId("sessUpd");
        updateData.setStatus(ReservationStatus.CONFIRMED);

        var opt = reservationService.updateReservation(saved.getId(), updateData);
        assertTrue(opt.isPresent());
        assertEquals(ReservationStatus.CONFIRMED, opt.get().getStatus());
    }

    @Test
    void deleteReservation_removesEntity() {
        Reservation reservation = new Reservation();
        reservation.setRoomId(roomId);
        reservation.setEtudiantId("studentDel");
        reservation.setStartTime(LocalDateTime.now().plusHours(10));
        reservation.setEndTime(LocalDateTime.now().plusHours(11));
        reservation.setExamenSessionId("sessDel");
        reservation.setStatus(ReservationStatus.PENDING);
        Reservation saved = reservationService.createReservation(reservation);

        boolean deleted = reservationService.deleteReservation(saved.getId());
        assertTrue(deleted);
        assertFalse(reservationService.getReservation(saved.getId()).isPresent());
    }
}
