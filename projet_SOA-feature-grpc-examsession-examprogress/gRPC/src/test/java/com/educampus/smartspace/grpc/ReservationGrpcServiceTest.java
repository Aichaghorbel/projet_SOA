package com.educampus.smartspace.grpc;

import com.educampus.smartspace.exception.ReservationOverlapException;
import com.educampus.smartspace.grpc.room.*;
import com.educampus.smartspace.model.Reservation;
import com.educampus.smartspace.model.ReservationStatus;
import com.educampus.smartspace.service.ReservationService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationGrpcService — gRPC Adapter Tests")
class ReservationGrpcServiceTest {

    @Mock
    private ReservationService reservationService;

    private ReservationGrpcService reservationGrpcService;

    @BeforeEach
    void setUp() {
        reservationGrpcService = new ReservationGrpcService(reservationService);
    }

    @Test
    @DisplayName("CreateReservation: Success returns CreateReservationResponse")
    void testCreateReservationSuccess() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 23, 12, 0);

        Reservation saved = Reservation.builder()
                .id(1L)
                .roomId(10L)
                .etudiantId("std-1")
                .startTime(start)
                .endTime(end)
                .status(ReservationStatus.CONFIRMED)
                .examenSessionId("sess-1")
                .build();

        when(reservationService.createReservation(any(Reservation.class))).thenReturn(saved);

        CreateReservationRequest request = CreateReservationRequest.newBuilder()
                .setRoomId(10L)
                .setUser("std-1")
                .setStartTime(start.toString())
                .setEndTime(end.toString())
                .setExamenSessionId("sess-1")
                .build();

        AtomicReference<CreateReservationResponse> responseRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        reservationGrpcService.createReservation(request, new StreamObserver<>() {
            @Override
            public void onNext(CreateReservationResponse value) {
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
        assertThat(responseRef.get().getReservation().getId()).isEqualTo(1L);
        assertThat(responseRef.get().getReservation().getUser()).isEqualTo("std-1");
    }

    @Test
    @DisplayName("CreateReservation: Overlap returns Status.ABORTED as per anti_overlap_rule.md")
    void testCreateReservationOverlapReturnsAborted() {
        when(reservationService.createReservation(any(Reservation.class)))
                .thenThrow(new ReservationOverlapException());

        CreateReservationRequest request = CreateReservationRequest.newBuilder()
                .setRoomId(10L)
                .setUser("std-2")
                .setStartTime("2026-11-23T10:30:00")
                .setEndTime("2026-11-23T12:30:00")
                .setExamenSessionId("sess-2")
                .build();

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        reservationGrpcService.createReservation(request, new StreamObserver<>() {
            @Override
            public void onNext(CreateReservationResponse value) {}

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertThat(errorRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException ex = (StatusRuntimeException) errorRef.get();
        // Verifies the anti-overlap rule status code is ABORTED
        assertThat(ex.getStatus().getCode()).isEqualTo(Status.Code.ABORTED);
        assertThat(ex.getStatus().getDescription()).contains("overlaps with an existing reservation");
    }

    @Test
    @DisplayName("GetReservation: Found reservation returns Reservation proto")
    void testGetReservationFound() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 23, 12, 0);

        Reservation res = Reservation.builder()
                .id(7L)
                .roomId(2L)
                .etudiantId("std-7")
                .startTime(start)
                .endTime(end)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationService.getReservation(7L)).thenReturn(Optional.of(res));

        AtomicReference<com.educampus.smartspace.grpc.room.Reservation> responseRef = new AtomicReference<>();

        reservationGrpcService.getReservation(GetReservationRequest.newBuilder().setId(7L).build(), new StreamObserver<>() {
            @Override
            public void onNext(com.educampus.smartspace.grpc.room.Reservation value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        });

        assertThat(responseRef.get()).isNotNull();
        assertThat(responseRef.get().getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("ListReservations: Returns all reservations")
    void testListReservations() {
        when(reservationService.listReservations()).thenReturn(List.of(
                Reservation.builder().id(1L).roomId(10L).etudiantId("s1").status(ReservationStatus.CONFIRMED).build(),
                Reservation.builder().id(2L).roomId(10L).etudiantId("s2").status(ReservationStatus.CONFIRMED).build()
        ));

        AtomicReference<ListReservationsResponse> responseRef = new AtomicReference<>();

        reservationGrpcService.listReservations(ListReservationsRequest.newBuilder().build(), new StreamObserver<>() {
            @Override
            public void onNext(ListReservationsResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        });

        assertThat(responseRef.get()).isNotNull();
        assertThat(responseRef.get().getReservationsCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("DeleteReservation: Existing reservation returns success=true")
    void testDeleteReservationSuccess() {
        when(reservationService.deleteReservation(1L)).thenReturn(true);

        AtomicReference<DeleteReservationResponse> responseRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        reservationGrpcService.deleteReservation(DeleteReservationRequest.newBuilder().setId(1L).build(), new StreamObserver<>() {
            @Override
            public void onNext(DeleteReservationResponse value) {
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
