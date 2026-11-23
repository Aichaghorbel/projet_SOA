package com.example.smartspace.service;

import com.example.smartspace.model.Room;
import com.example.smartspace.model.Reservation;
import com.example.smartspace.model.ReservationStatus;
import com.example.smartspace.repository.RoomRepository;
import com.example.smartspace.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Optional<Room> getRoom(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> listRooms() {
        return roomRepository.findAll();
    }

    @Transactional
    public Optional<Room> updateRoom(Long id, Room updated) {
        return roomRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setCapacity(updated.getCapacity());
            existing.setLocation(updated.getLocation());
            return roomRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteRoom(Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Helper to check reservation overlap (used by reservation service)
    public boolean isOverlap(Long roomId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return isOverlap(roomId, start, end, null);
    }

    public boolean isOverlap(Long roomId, java.time.LocalDateTime start, java.time.LocalDateTime end, Long excludeReservationId) {
        return reservationRepository.findByRoomId(roomId).stream()
                .filter(r -> excludeReservationId == null || !r.getId().equals(excludeReservationId))
                .filter(r -> r.getStatus() != ReservationStatus.CANCELED)
                .anyMatch(r -> start.isBefore(r.getEndTime()) && end.isAfter(r.getStartTime()));
    }
}
