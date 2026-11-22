package com.example.smartspace.service;

import com.example.smartspace.model.Room;
import com.example.smartspace.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    private Long createdRoomId;

    @BeforeEach
    void setUp() {
        // Ensure a clean room exists for each test
        Room room = new Room();
        room.setName("Test Room");
        room.setCapacity(20);
        room.setLocation("2nd floor");
        Room saved = roomRepository.save(room);
        createdRoomId = saved.getId();
    }

    @Test
    void createRoom_success() {
        Room newRoom = new Room();
        newRoom.setName("New Room");
        newRoom.setCapacity(15);
        newRoom.setLocation("3rd floor");
        Room saved = roomService.createRoom(newRoom);
        assertNotNull(saved.getId());
        assertEquals("New Room", saved.getName());
    }

    @Test
    void getRoom_existing() {
        assertTrue(roomService.getRoom(createdRoomId).isPresent());
    }

    @Test
    void listRooms_containsCreated() {
        List<Room> rooms = roomService.listRooms();
        assertTrue(rooms.stream().anyMatch(r -> r.getId().equals(createdRoomId)));
    }

    @Test
    void updateRoom_changesFields() {
        Room updated = new Room();
        updated.setName("Updated Name");
        updated.setCapacity(30);
        updated.setLocation("Updated Location");
        var opt = roomService.updateRoom(createdRoomId, updated);
        assertTrue(opt.isPresent());
        Room after = opt.get();
        assertEquals("Updated Name", after.getName());
        assertEquals(30, after.getCapacity());
        assertEquals("Updated Location", after.getLocation());
    }

    @Test
    void deleteRoom_removesEntity() {
        boolean deleted = roomService.deleteRoom(createdRoomId);
        assertTrue(deleted);
        assertFalse(roomService.getRoom(createdRoomId).isPresent());
    }
}
