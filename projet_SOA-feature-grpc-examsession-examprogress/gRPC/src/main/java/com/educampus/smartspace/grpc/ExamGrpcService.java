package com.educampus.smartspace.grpc;

import com.educampus.smartspace.exception.ExamSessionNotFoundException;
import com.educampus.smartspace.exception.InvalidExamSubmissionException;
import com.educampus.smartspace.exception.QuestionNotFoundException;
import com.educampus.smartspace.grpc.exam.*;
import com.educampus.smartspace.model.ProgressStatus;
import com.educampus.smartspace.service.ExamService;
import com.educampus.smartspace.service.ExamService.ExamProgressEventDto;
import com.educampus.smartspace.service.ExamService.ExamResult;
import com.educampus.smartspace.service.ExamService.StudentAnswerInput;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * gRPC adapter for ExamService.
 * Translates proto messages to domain DTOs and vice-versa.
 * Handles gRPC status codes for all error cases.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class ExamGrpcService extends ExamServiceGrpc.ExamServiceImplBase {

    private final ExamService examService;

    // =====================================================================
    // SubmitExam — Unary RPC
    // =====================================================================

    @Override
    public void submitExam(ExamSubmissionRequest request,
                           StreamObserver<ExamSubmissionResponse> responseObserver) {
        log.info("gRPC SubmitExam: session={}, exam={}, student={}",
                request.getExamSessionId(), request.getExamId(), request.getStudentId());

        try {
            validateRequest(request);

            ExamResult result = examService.submitExam(
                    request.getExamSessionId(),
                    request.getStudentId(),
                    request.getExamId(),
                    mapInputs(request.getAnswersList())
            );

            responseObserver.onNext(buildResponse(result));
            responseObserver.onCompleted();
            log.info("SubmitExam completed: session={}, grade={}", result.examSessionId(), result.grade());

        } catch (InvalidExamSubmissionException e) {
            log.warn("Invalid submission: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ExamSessionNotFoundException | QuestionNotFoundException e) {
            log.warn("Entity not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Data integrity conflict: {}", e.getMessage());
            String desc = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Conflict with existing database records: " + desc).asRuntimeException());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Precondition/argument error: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Internal error in submitExam", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal error: " + e.getMessage()).asRuntimeException());
        }
    }

    // =====================================================================
    // StreamExamProgress — Server-Streaming RPC
    // =====================================================================

    @Override
    public void streamExamProgress(ExamSubmissionRequest request,
                                   StreamObserver<ExamProgressEvent> responseObserver) {
        log.info("gRPC StreamExamProgress: session={}, exam={}, student={}, answers={}",
                request.getExamSessionId(), request.getExamId(), request.getStudentId(),
                request.getAnswersCount());

        try {
            validateRequest(request);

            examService.streamExamProgress(
                    request.getExamSessionId(),
                    request.getStudentId(),
                    request.getExamId(),
                    mapInputs(request.getAnswersList()),
                    eventDto -> {
                        ExamProgressEvent event = buildProgressEvent(eventDto);
                        log.debug("Streaming event: question={}, status={}, pct={}%",
                                eventDto.questionNumber(), eventDto.status(), eventDto.progressPercentage());
                        responseObserver.onNext(event);
                    }
            );

            responseObserver.onCompleted();
            log.info("StreamExamProgress completed: session={}", request.getExamSessionId());

        } catch (InvalidExamSubmissionException e) {
            log.warn("Invalid submission: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ExamSessionNotFoundException | QuestionNotFoundException e) {
            log.warn("Entity not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("Data integrity conflict: {}", e.getMessage());
            String desc = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Conflict with existing database records: " + desc).asRuntimeException());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Precondition/argument error: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Internal error in streamExamProgress", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal error: " + e.getMessage()).asRuntimeException());
        }
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    private void validateRequest(ExamSubmissionRequest request) {
        if (request.getStudentId() <= 0) {
            throw new InvalidExamSubmissionException("Student ID must be a positive number");
        }
        if (request.getExamId() <= 0) {
            throw new InvalidExamSubmissionException("Exam ID must be a positive number");
        }
        if (request.getAnswersCount() == 0) {
            throw new InvalidExamSubmissionException("At least one answer is required");
        }
        for (var answer : request.getAnswersList()) {
            if (answer.getQuestionId() <= 0) {
                throw new InvalidExamSubmissionException("Question ID must be a positive number");
            }
        }
    }

    private List<StudentAnswerInput> mapInputs(List<com.educampus.smartspace.grpc.exam.StudentAnswerInput> protoInputs) {
        return protoInputs.stream()
                .map(a -> new StudentAnswerInput(
                        a.getQuestionId(),
                        a.getSelectedOptionId() > 0 ? a.getSelectedOptionId() : null,
                        a.getTextResponse().isBlank() ? null : a.getTextResponse()))
                .collect(Collectors.toList());
    }

    private ExamSubmissionResponse buildResponse(ExamResult result) {
        return ExamSubmissionResponse.newBuilder()
                .setExamSessionId(result.examSessionId())
                .setTotalScore(result.totalScore())
                .setMaxScore(result.maxScore())
                .setGrade(result.grade())
                .setPassed(result.passed())
                .build();
    }

    private ExamProgressEvent buildProgressEvent(ExamProgressEventDto dto) {
        return ExamProgressEvent.newBuilder()
                .setExamSessionId(dto.examSessionId())
                .setQuestionId(dto.questionId())
                .setQuestionNumber(dto.questionNumber())
                .setStatus(mapStatus(dto.status()))
                .setPointsEarned(dto.pointsEarned())
                .setProgressPercentage(dto.progressPercentage())
                .setTimestamp(dto.timestamp().toString())
                .setIsFinal(dto.isFinal())
                .build();
    }

    private com.educampus.smartspace.grpc.exam.ProgressStatus mapStatus(ProgressStatus modelStatus) {
        if (modelStatus == null) return com.educampus.smartspace.grpc.exam.ProgressStatus.PROGRESS_STATUS_UNSPECIFIED;
        return switch (modelStatus) {
            case CORRECTING -> com.educampus.smartspace.grpc.exam.ProgressStatus.CORRECTING;
            case CORRECTED -> com.educampus.smartspace.grpc.exam.ProgressStatus.CORRECTED;
            case ERROR -> com.educampus.smartspace.grpc.exam.ProgressStatus.ERROR;
        };
    }
}
