package com.educampus.smartspace.grpc;

import com.educampus.smartspace.exception.ReservationOverlapException;
import com.educampus.smartspace.grpc.room.*;
import com.educampus.smartspace.model.Reservation;
import com.educampus.smartspace.model.ReservationStatus;
import com.educampus.smartspace.service.ReservationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * gRPC adapter for ReservationService.
 * Validates reservations and enforces the anti-overlap rule (Status.ABORTED).
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ReservationGrpcService extends ReservationServiceGrpc.ReservationServiceImplBase {

    private final ReservationService reservationService;

    @Override
    public void createReservation(CreateReservationRequest request, StreamObserver<CreateReservationResponse> responseObserver) {
        log.info("gRPC CreateReservation: roomId={}, user={}, start={}, end={}",
                request.getRoomId(), request.getUser(), request.getStartTime(), request.getEndTime());
        try {
            if (request.getRoomId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Room ID must be positive").asRuntimeException());
                return;
            }
            if (request.getUser().isBlank()) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("User / Student ID cannot be empty").asRuntimeException());
                return;
            }

            LocalDateTime start;
            LocalDateTime end;
            try {
                start = LocalDateTime.parse(request.getStartTime());
                end = LocalDateTime.parse(request.getEndTime());
            } catch (DateTimeParseException e) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid ISO-8601 timestamp format: " + e.getMessage()).asRuntimeException());
                return;
            }

            if (!start.isBefore(end)) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Start time must be before end time").asRuntimeException());
                return;
            }

            Reservation reservation = Reservation.builder()
                    .roomId(request.getRoomId())
                    .etudiantId(request.getUser())
                    .startTime(start)
                    .endTime(end)
                    .examenSessionId(request.getExamenSessionId().isBlank() ? "default-session" : request.getExamenSessionId())
                    .status(ReservationStatus.CONFIRMED)
                    .build();

            Reservation saved = reservationService.createReservation(reservation);

            CreateReservationResponse response = CreateReservationResponse.newBuilder()
                    .setReservation(RoomGrpcService.toProtoReservation(saved))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (ReservationOverlapException e) {
            log.warn("Reservation overlap detected: {}", e.getMessage());
            // Conforms to anti_overlap_rule.md: Status.ABORTED
            responseObserver.onError(Status.ABORTED.withDescription("Reservation time overlaps with an existing reservation").asRuntimeException());
        } catch (IllegalArgumentException e) {
            log.warn("Illegal argument in createReservation: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Internal error in createReservation", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal error: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getReservation(GetReservationRequest request, StreamObserver<com.educampus.smartspace.grpc.room.Reservation> responseObserver) {
        log.info("gRPC GetReservation: id={}", request.getId());
        try {
            if (request.getId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Reservation ID must be positive").asRuntimeException());
                return;
            }

            Optional<Reservation> resOpt = reservationService.getReservation(request.getId());
            if (resOpt.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Reservation not found: id=" + request.getId()).asRuntimeException());
                return;
            }

            responseObserver.onNext(RoomGrpcService.toProtoReservation(resOpt.get()));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting reservation", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listReservations(ListReservationsRequest request, StreamObserver<ListReservationsResponse> responseObserver) {
        log.info("gRPC ListReservations: roomId={}", request.getRoomId());
        try {
            List<Reservation> reservations;
            if (request.getRoomId() > 0) {
                reservations = reservationService.listReservationsByRoom(request.getRoomId());
            } else {
                reservations = reservationService.listReservations();
            }

            ListReservationsResponse.Builder builder = ListReservationsResponse.newBuilder();
            for (Reservation r : reservations) {
                builder.addReservations(RoomGrpcService.toProtoReservation(r));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error listing reservations", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteReservation(DeleteReservationRequest request, StreamObserver<DeleteReservationResponse> responseObserver) {
        log.info("gRPC DeleteReservation: id={}", request.getId());
        try {
            if (request.getId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Reservation ID must be positive").asRuntimeException());
                return;
            }

            boolean success = reservationService.deleteReservation(request.getId());
            if (!success) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Reservation not found: id=" + request.getId()).asRuntimeException());
                return;
            }

            DeleteReservationResponse response = DeleteReservationResponse.newBuilder()
                    .setSuccess(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting reservation", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Error: " + e.getMessage()).asRuntimeException());
        }
    }
}
