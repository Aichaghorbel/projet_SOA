package com.educampus.smartspace.service;

import com.educampus.smartspace.model.Room;
import com.educampus.smartspace.model.ReservationStatus;
import com.educampus.smartspace.repository.RoomRepository;
import com.educampus.smartspace.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public Optional<Room> getRoom(Long id) {
        return roomRepository.findByIdWithReservations(id);
    }

    @Transactional(readOnly = true)
    public List<Room> listRooms() {
        return roomRepository.findAllWithReservations();
    }

    @Transactional
    public Optional<Room> updateRoom(Long id, Room updated) {
        return roomRepository.findById(id).map(existing -> {
            if (updated.getName() != null && !updated.getName().isBlank()) {
                existing.setName(updated.getName());
            }
            if (updated.getCapacity() > 0) {
                existing.setCapacity(updated.getCapacity());
            }
            if (updated.getLocation() != null && !updated.getLocation().isBlank()) {
                existing.setLocation(updated.getLocation());
            }
            return roomRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteRoom(Long id) {
        if (roomRepository.existsById(id)) {
            // Delete associated reservations first to prevent foreign key errors
            var reservations = reservationRepository.findByRoomId(id);
            if (reservations != null && !reservations.isEmpty()) {
                reservationRepository.deleteAll(reservations);
            }
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Helper to check reservation overlap (used by reservation service)
    public boolean isOverlap(Long roomId, LocalDateTime start, LocalDateTime end) {
        return isOverlap(roomId, start, end, null);
    }

    public boolean isOverlap(Long roomId, LocalDateTime start, LocalDateTime end, Long excludeReservationId) {
        return reservationRepository.findByRoomId(roomId).stream()
                .filter(r -> excludeReservationId == null || !r.getId().equals(excludeReservationId))
                .filter(r -> r.getStatus() != ReservationStatus.CANCELED)
                .anyMatch(r -> start.isBefore(r.getEndTime()) && end.isAfter(r.getStartTime()));
    }
}
