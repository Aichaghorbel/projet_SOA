package com.educampus.smartspace.grpc;

import com.educampus.smartspace.grpc.room.*;
import com.educampus.smartspace.model.Room;
import com.educampus.smartspace.service.RoomService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomGrpcService — gRPC Adapter Tests")
class RoomGrpcServiceTest {

    @Mock
    private RoomService roomService;

    private RoomGrpcService roomGrpcService;

    @BeforeEach
    void setUp() {
        roomGrpcService = new RoomGrpcService(roomService);
    }

    @Test
    @DisplayName("CreateRoom: Valid request returns CreateRoomResponse")
    void testCreateRoomSuccess() {
        Room created = Room.builder()
                .id(1L)
                .name("Amphi A")
                .capacity(100)
                .location("Floor 1")
                .isActive(true)
                .build();

        when(roomService.createRoom(any(Room.class))).thenReturn(created);

        CreateRoomRequest request = CreateRoomRequest.newBuilder()
                .setName("Amphi A")
                .setCapacity(100)
                .setLocation("Floor 1")
                .build();

        AtomicReference<CreateRoomResponse> responseRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        roomGrpcService.createRoom(request, new StreamObserver<>() {
            @Override
            public void onNext(CreateRoomResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(completed.get()).isTrue();
        assertThat(responseRef.get()).isNotNull();
        assertThat(responseRef.get().getRoom().getId()).isEqualTo(1L);
        assertThat(responseRef.get().getRoom().getName()).isEqualTo("Amphi A");
    }

    @Test
    @DisplayName("GetRoom: Found room returns Room proto")
    void testGetRoomFound() {
        Room room = Room.builder()
                .id(5L)
                .name("Lab 1")
                .capacity(25)
                .location("Bldg B")
                .isActive(true)
                .build();

        when(roomService.getRoom(5L)).thenReturn(Optional.of(room));

        AtomicReference<com.educampus.smartspace.grpc.room.Room> responseRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        roomGrpcService.getRoom(GetRoomRequest.newBuilder().setId(5L).build(), new StreamObserver<>() {
            @Override
            public void onNext(com.educampus.smartspace.grpc.room.Room value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(completed.get()).isTrue();
        assertThat(responseRef.get().getName()).isEqualTo("Lab 1");
    }

    @Test
    @DisplayName("GetRoom: Missing room returns NOT_FOUND")
    void testGetRoomNotFound() {
        when(roomService.getRoom(99L)).thenReturn(Optional.empty());

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        roomGrpcService.getRoom(GetRoomRequest.newBuilder().setId(99L).build(), new StreamObserver<>() {
            @Override
            public void onNext(com.educampus.smartspace.grpc.room.Room value) {}

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertThat(errorRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errorRef.get();
        assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    @DisplayName("ListRooms: Returns all rooms")
    void testListRooms() {
        when(roomService.listRooms()).thenReturn(List.of(
                Room.builder().id(1L).name("Room 1").capacity(10).build(),
                Room.builder().id(2L).name("Room 2").capacity(20).build()
        ));

        AtomicReference<ListRoomsResponse> responseRef = new AtomicReference<>();

        roomGrpcService.listRooms(ListRoomsRequest.newBuilder().build(), new StreamObserver<>() {
            @Override
            public void onNext(ListRoomsResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        });

        assertThat(responseRef.get()).isNotNull();
        assertThat(responseRef.get().getRoomsCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("UpdateRoom: Valid request updates and returns updated room")
    void testUpdateRoomSuccess() {
        Room updated = Room.builder()
                .id(1L)
                .name("Updated Salle A")
                .capacity(50)
                .location("Floor 2")
                .isActive(true)
                .build();

        when(roomService.updateRoom(eq(1L), any(Room.class))).thenReturn(Optional.of(updated));

        UpdateRoomRequest request = UpdateRoomRequest.newBuilder()
                .setId(1L)
                .setName("Updated Salle A")
                .setCapacity(50)
                .setLocation("Floor 2")
                .build();

        AtomicReference<UpdateRoomResponse> responseRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        roomGrpcService.updateRoom(request, new StreamObserver<>() {
            @Override
            public void onNext(UpdateRoomResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(completed.get()).isTrue();
        assertThat(responseRef.get().getRoom().getName()).isEqualTo("Updated Salle A");
        assertThat(responseRef.get().getRoom().getCapacity()).isEqualTo(50);
    }

    @Test
    @DisplayName("DeleteRoom: Existing room returns success=true")
    void testDeleteRoomSuccess() {
        when(roomService.deleteRoom(1L)).thenReturn(true);

        AtomicReference<DeleteRoomResponse> responseRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        roomGrpcService.deleteRoom(DeleteRoomRequest.newBuilder().setId(1L).build(), new StreamObserver<>() {
            @Override
            public void onNext(DeleteRoomResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(completed.get()).isTrue();
        assertThat(responseRef.get().getSuccess()).isTrue();
    }
}
