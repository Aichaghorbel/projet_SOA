package com.educampus.smartspace.grpc;

import com.educampus.smartspace.grpc.room.*;
import com.educampus.smartspace.model.Room;
import com.educampus.smartspace.model.Reservation;
import com.educampus.smartspace.service.RoomService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Optional;

/**
 * gRPC adapter for RoomService.
 * Translates proto messages to domain models and vice-versa.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RoomGrpcService extends RoomServiceGrpc.RoomServiceImplBase {

    private final RoomService roomService;

    @Override
    public void createRoom(CreateRoomRequest request, StreamObserver<CreateRoomResponse> responseObserver) {
        log.info("gRPC CreateRoom: name={}, capacity={}, location={}",
                request.getName(), request.getCapacity(), request.getLocation());
        try {
            if (request.getName().isBlank()) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Room name cannot be empty").asRuntimeException());
                return;
            }
            if (request.getCapacity() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Room capacity must be greater than 0").asRuntimeException());
                return;
            }

            Room room = Room.builder()
                    .name(request.getName())
                    .capacity(request.getCapacity())
                    .location(request.getLocation())
                    .isActive(true)
                    .build();

            Room created = roomService.createRoom(room);
            CreateRoomResponse response = CreateRoomResponse.newBuilder()
                    .setRoom(toProtoRoom(created))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating room", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getRoom(GetRoomRequest request, StreamObserver<com.educampus.smartspace.grpc.room.Room> responseObserver) {
        log.info("gRPC GetRoom: id={}", request.getId());
        try {
            if (request.getId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Room ID must be positive").asRuntimeException());
                return;
            }

            Optional<Room> roomOpt = roomService.getRoom(request.getId());
            if (roomOpt.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Room not found: id=" + request.getId()).asRuntimeException());
                return;
            }

            responseObserver.onNext(toProtoRoom(roomOpt.get()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting room", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updateRoom(UpdateRoomRequest request, StreamObserver<UpdateRoomResponse> responseObserver) {
        log.info("gRPC UpdateRoom: id={}, name={}, capacity={}", request.getId(), request.getName(), request.getCapacity());
        try {
            if (request.getId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Room ID must be positive").asRuntimeException());
                return;
            }

            Room updateData = Room.builder()
                    .name(request.getName())
                    .capacity(request.getCapacity())
                    .location(request.getLocation())
                    .build();

            Optional<Room> updatedOpt = roomService.updateRoom(request.getId(), updateData);
            if (updatedOpt.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Room not found: id=" + request.getId()).asRuntimeException());
                return;
            }

            UpdateRoomResponse response = UpdateRoomResponse.newBuilder()
                    .setRoom(toProtoRoom(updatedOpt.get()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating room", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteRoom(DeleteRoomRequest request, StreamObserver<DeleteRoomResponse> responseObserver) {
        log.info("gRPC DeleteRoom: id={}", request.getId());
        try {
            if (request.getId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Room ID must be positive").asRuntimeException());
                return;
            }

            boolean success = roomService.deleteRoom(request.getId());
            if (!success) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Room not found: id=" + request.getId()).asRuntimeException());
                return;
            }

            DeleteRoomResponse response = DeleteRoomResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting room", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listRooms(ListRoomsRequest request, StreamObserver<ListRoomsResponse> responseObserver) {
        log.info("gRPC ListRooms");
        try {
            List<Room> rooms = roomService.listRooms();
            ListRoomsResponse.Builder builder = ListRoomsResponse.newBuilder();
            for (Room r : rooms) {
                builder.addRooms(toProtoRoom(r));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error listing rooms", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }

    public static com.educampus.smartspace.grpc.room.Room toProtoRoom(Room room) {
        com.educampus.smartspace.grpc.room.Room.Builder builder = com.educampus.smartspace.grpc.room.Room.newBuilder()
                .setId(room.getId() != null ? room.getId() : 0L)
                .setName(room.getName() != null ? room.getName() : "")
                .setCapacity(room.getCapacity())
                .setLocation(room.getLocation() != null ? room.getLocation() : "")
                .setIsActive(room.isActive());

        try {
            if (room.getReservations() != null) {
                for (Reservation res : room.getReservations()) {
                    builder.addReservations(toProtoReservation(res));
                }
            }
        } catch (Exception e) {
            log.warn("Could not lazily load reservations for room id={}: {}", room.getId(), e.getMessage());
        }
        return builder.build();
    }

    public static com.educampus.smartspace.grpc.room.Reservation toProtoReservation(Reservation res) {
        return com.educampus.smartspace.grpc.room.Reservation.newBuilder()
                .setId(res.getId() != null ? res.getId() : 0L)
                .setRoomId(res.getRoomId() != null ? res.getRoomId() : 0L)
                .setUser(res.getEtudiantId() != null ? res.getEtudiantId() : "")
                .setStartTime(res.getStartTime() != null ? res.getStartTime().toString() : "")
                .setEndTime(res.getEndTime() != null ? res.getEndTime().toString() : "")
                .setStatus(res.getStatus() != null ? res.getStatus().name() : "")
                .setExamenSessionId(res.getExamenSessionId() != null ? res.getExamenSessionId() : "")
                .build();
    }
}
