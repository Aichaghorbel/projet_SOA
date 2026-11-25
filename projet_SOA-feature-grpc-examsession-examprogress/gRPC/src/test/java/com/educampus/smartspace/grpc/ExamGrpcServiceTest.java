package com.educampus.smartspace.grpc;

import com.educampus.smartspace.exception.ExamSessionNotFoundException;
import com.educampus.smartspace.exception.QuestionNotFoundException;
import com.educampus.smartspace.grpc.exam.*;
import com.educampus.smartspace.model.ProgressStatus;
import com.educampus.smartspace.service.ExamService;
import com.educampus.smartspace.service.ExamService.ExamProgressEventDto;
import com.educampus.smartspace.service.ExamService.ExamResult;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamGrpcService — gRPC Adapter Tests")
class ExamGrpcServiceTest {

    @Mock
    private ExamService examService;

    private ExamGrpcService examGrpcService;

    @BeforeEach
    void setUp() {
        examGrpcService = new ExamGrpcService(examService);
    }

    // =========================================================================
    // SubmitExam (Unary RPC) Tests
    // =========================================================================

    @Test
    @DisplayName("SubmitExam: Valid request returns ExamSubmissionResponse with scores and grade")
    void testSubmitExamSuccess() {
        when(examService.submitExam(eq(100L), eq(42L), eq(101L), anyList()))
                .thenReturn(new ExamResult(100L, 18.0, 20.0, "A", true));

        ExamSubmissionRequest request = ExamSubmissionRequest.newBuilder()
                .setExamSessionId(100L)
                .setExamId(101L)
                .setStudentId(42L)
                .addAnswers(StudentAnswerInput.newBuilder()
                        .setQuestionId(1L)
                        .setSelectedOptionId(10L)
                        .build())
                .addAnswers(StudentAnswerInput.newBuilder()
                        .setQuestionId(2L)
                        .setTextResponse("grpc streaming")
                        .build())
                .build();

        AtomicReference<ExamSubmissionResponse> responseRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        examGrpcService.submitExam(request, new StreamObserver<>() {
            @Override
            public void onNext(ExamSubmissionResponse value) {
                responseRef.set(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(completed.get()).isTrue();
        assertThat(responseRef.get()).isNotNull();
        assertThat(responseRef.get().getExamSessionId()).isEqualTo(100L);
        assertThat(responseRef.get().getTotalScore()).isEqualTo(18.0);
        assertThat(responseRef.get().getMaxScore()).isEqualTo(20.0);
        assertThat(responseRef.get().getGrade()).isEqualTo("A");
        assertThat(responseRef.get().getPassed()).isTrue();
    }

    @Test
    @DisplayName("SubmitExam: studentId <= 0 returns INVALID_ARGUMENT")
    void testSubmitExamInvalidStudent() {
        ExamSubmissionRequest request = ExamSubmissionRequest.newBuilder()
                .setExamSessionId(100L)
                .setExamId(101L)
                .setStudentId(0L)
                .addAnswers(StudentAnswerInput.newBuilder().setQuestionId(1L).build())
                .build();

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        examGrpcService.submitExam(request, new StreamObserver<>() {
            @Override
            public void onNext(ExamSubmissionResponse value) {}

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertThat(errorRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException sre = (StatusRuntimeException) errorRef.get();
        assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    @Test
    @DisplayName("SubmitExam: Empty answers list returns INVALID_ARGUMENT")
    void testSubmitExamNoAnswers() {
        ExamSubmissionRequest request = ExamSubmissionRequest.newBuilder()
                .setExamSessionId(100L)
                .setExamId(101L)
                .setStudentId(42L)
                .build();

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        examGrpcService.submitExam(request, new StreamObserver<>() {
            @Override
            public void onNext(ExamSubmissionResponse value) {}

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertThat(errorRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException sre = (StatusRuntimeException) errorRef.get();
        assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
    }

    @Test
    @DisplayName("SubmitExam: Session not found returns NOT_FOUND")
    void testSubmitExamSessionNotFound() {
        when(examService.submitExam(anyLong(), anyLong(), anyLong(), anyList()))
                .thenThrow(new ExamSessionNotFoundException("Session not found: 999"));

        ExamSubmissionRequest request = ExamSubmissionRequest.newBuilder()
                .setExamSessionId(999L)
                .setExamId(101L)
                .setStudentId(42L)
                .addAnswers(StudentAnswerInput.newBuilder().setQuestionId(1L).build())
                .build();

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        examGrpcService.submitExam(request, new StreamObserver<>() {
            @Override
            public void onNext(ExamSubmissionResponse value) {}

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertThat(errorRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException sre = (StatusRuntimeException) errorRef.get();
        assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }

    // =========================================================================
    // StreamExamProgress (Server-Streaming RPC) Tests
    // =========================================================================

    @Test
    @DisplayName("StreamExamProgress: Server-streaming emits progress events sequentially and completes")
    void testStreamExamProgressSuccess() {
        doAnswer(invocation -> {
            Consumer<ExamProgressEventDto> consumer = invocation.getArgument(4);
            consumer.accept(new ExamProgressEventDto(
                    100L, 1L, 1, ProgressStatus.CORRECTED, 5.0, 50, Instant.now(), false));
            consumer.accept(new ExamProgressEventDto(
                    100L, 2L, 2, ProgressStatus.CORRECTED, 5.0, 100, Instant.now(), true));
            return new ExamResult(100L, 10.0, 10.0, "A", true);
        }).when(examService).streamExamProgress(eq(100L), eq(42L), eq(101L), anyList(), any());

        ExamSubmissionRequest request = ExamSubmissionRequest.newBuilder()
                .setExamSessionId(100L)
                .setExamId(101L)
                .setStudentId(42L)
                .addAnswers(StudentAnswerInput.newBuilder().setQuestionId(1L).setSelectedOptionId(10L).build())
                .addAnswers(StudentAnswerInput.newBuilder().setQuestionId(2L).setSelectedOptionId(20L).build())
                .build();

        List<ExamProgressEvent> events = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        examGrpcService.streamExamProgress(request, new StreamObserver<>() {
            @Override
            public void onNext(ExamProgressEvent value) {
                events.add(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        });

        assertThat(completed.get()).isTrue();
        assertThat(events).hasSize(2);

        assertThat(events.get(0).getQuestionNumber()).isEqualTo(1);
        assertThat(events.get(0).getProgressPercentage()).isEqualTo(50);
        assertThat(events.get(0).getIsFinal()).isFalse();

        assertThat(events.get(1).getQuestionNumber()).isEqualTo(2);
        assertThat(events.get(1).getProgressPercentage()).isEqualTo(100);
        assertThat(events.get(1).getIsFinal()).isTrue();
    }

    @Test
    @DisplayName("StreamExamProgress: Question not found returns NOT_FOUND")
    void testStreamExamProgressQuestionNotFound() {
        when(examService.streamExamProgress(anyLong(), anyLong(), anyLong(), anyList(), any()))
                .thenThrow(new QuestionNotFoundException("Question not found: 999"));

        ExamSubmissionRequest request = ExamSubmissionRequest.newBuilder()
                .setExamSessionId(100L)
                .setExamId(101L)
                .setStudentId(42L)
                .addAnswers(StudentAnswerInput.newBuilder().setQuestionId(999L).build())
                .build();

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        examGrpcService.streamExamProgress(request, new StreamObserver<>() {
            @Override
            public void onNext(ExamProgressEvent value) {}

            @Override
            public void onError(Throwable t) {
                errorRef.set(t);
            }

            @Override
            public void onCompleted() {}
        });

        assertThat(errorRef.get()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException sre = (StatusRuntimeException) errorRef.get();
        assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }
}
