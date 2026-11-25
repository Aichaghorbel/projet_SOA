package com.educampus.smartspace.repository;

import com.educampus.smartspace.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.reservations")
    List<Room> findAllWithReservations();

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.reservations WHERE r.id = :id")
    Optional<Room> findByIdWithReservations(@Param("id") Long id);
}
