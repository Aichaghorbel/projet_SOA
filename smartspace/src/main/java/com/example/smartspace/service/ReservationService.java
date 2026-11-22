package com.example.smartspace.service;

import com.example.smartspace.model.Reservation;
import com.example.smartspace.model.Room;
import com.example.smartspace.repository.ReservationRepository;
import com.example.smartspace.repository.RoomRepository;
import com.example.smartspace.exception.ReservationOverlapException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService; // to reuse overlap check if needed

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              RoomService roomService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // Verify room exists
        Long roomId = reservation.getRoomId();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        // Check overlap using RoomService helper
        if (roomService.isOverlap(roomId, reservation.getStartTime(), reservation.getEndTime())) {
            throw new ReservationOverlapException();
        }
        // Associate roomId (entity stores only id, not a Room object)
        return reservationRepository.save(reservation);
    }

    public Optional<Reservation> getReservation(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> listReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Optional<Reservation> updateReservation(Long id, Reservation updated) {
        return reservationRepository.findById(id).map(existing -> {
            // Update mutable fields
            existing.setStartTime(updated.getStartTime());
            existing.setEndTime(updated.getEndTime());
            existing.setEtudiantId(updated.getEtudiantId());
            existing.setExamenSessionId(updated.getExamenSessionId());
            existing.setStatus(updated.getStatus());
            // Overlap check if times changed
            if (roomService.isOverlap(existing.getRoomId(), existing.getStartTime(), existing.getEndTime(), existing.getId())) {
                throw new ReservationOverlapException();
            }
            return reservationRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteReservation(Long id) {
        if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
