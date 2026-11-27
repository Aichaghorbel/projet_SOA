package com.educampus.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.educampus.academic.dto.ResultRequest;
import com.educampus.academic.dto.ResultResponse;
import com.educampus.academic.dto.ResultUpdateRequest;
import com.educampus.academic.entity.Enrollment;
import com.educampus.academic.entity.EnrollmentStatus;
import com.educampus.academic.entity.Exam;
import com.educampus.academic.entity.Result;
import com.educampus.academic.entity.ResultSource;
import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.entity.SessionType;
import com.educampus.academic.exception.DuplicateResultException;
import com.educampus.academic.exception.EnrollmentNotFoundException;
import com.educampus.academic.exception.ResultConflictException;
import com.educampus.academic.exception.ResultNotFoundException;
import com.educampus.academic.repository.EnrollmentRepository;
import com.educampus.academic.repository.ExamRepository;
import com.educampus.academic.repository.ResultRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock private ResultRepository resultRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private ExamRepository examRepository;

    @InjectMocks
    private ResultService service;

    private Enrollment enrollment(EnrollmentStatus status) {
        Enrollment e = new Enrollment();
        e.setStudentId(10L);
        e.setCourseId(20L);
        e.setAcademicYear("2026-2027");
        e.setStatus(status);
        ReflectionTestUtils.setField(e, "id", 1L);
        return e;
    }

    private Exam exam(Long courseId, String maxGrade) {
        Exam x = new Exam();
        x.setCourseId(courseId);
        x.setMaxGrade(new BigDecimal(maxGrade));
        ReflectionTestUtils.setField(x, "id", 5L);
        return x;
    }

    private Result result(ResultStatus status) {
        Result r = new Result();
        r.setEnrollmentId(1L);
        r.setExamId(5L);
        r.setGrade(new BigDecimal("12"));
        r.setSessionType(SessionType.NORMAL);
        r.setStatus(status);
        ReflectionTestUtils.setField(r, "id", 100L);
        return r;
    }

    @Test
    void createSavesDraftByDefaultWithManualSource() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(EnrollmentStatus.CONFIRMED)));
        when(resultRepository.existsByEnrollmentIdAndExamIdAndSessionType(1L, null, SessionType.NORMAL))
                .thenReturn(false);
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultResponse response = service.create(
                new ResultRequest(1L, null, new BigDecimal("12"), SessionType.NORMAL, null, null));

        assertThat(response.status()).isEqualTo(ResultStatus.DRAFT);
        assertThat(response.source()).isEqualTo(ResultSource.MANUAL);
        assertThat(response.publishedAt()).isNull();
    }

    @Test
    void createPublishedResultSetsPublishedAt() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(EnrollmentStatus.CONFIRMED)));
        when(resultRepository.existsByEnrollmentIdAndExamIdAndSessionType(1L, null, SessionType.NORMAL))
                .thenReturn(false);
        when(resultRepository.save(any(Result.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultResponse response = service.create(
                new ResultRequest(1L, null, new BigDecimal("12"), SessionType.NORMAL, ResultStatus.PUBLISHED, null));

        assertThat(response.status()).isEqualTo(ResultStatus.PUBLISHED);
        assertThat(response.publishedAt()).isNotNull();
    }

    @Test
    void createRejectsCancelledEnrollment() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(EnrollmentStatus.CANCELLED)));

        assertThatThrownBy(() -> service.create(
                new ResultRequest(1L, null, new BigDecimal("12"), SessionType.NORMAL, null, null)))
                .isInstanceOf(ResultConflictException.class);
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void createRejectsExamFromAnotherCourse() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(EnrollmentStatus.CONFIRMED)));
        when(examRepository.findById(5L)).thenReturn(Optional.of(exam(99L, "20")));

        assertThatThrownBy(() -> service.create(
                new ResultRequest(1L, 5L, new BigDecimal("12"), SessionType.NORMAL, null, null)))
                .isInstanceOf(ResultConflictException.class);
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void createRejectsGradeAboveExamMax() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(EnrollmentStatus.CONFIRMED)));
        when(examRepository.findById(5L)).thenReturn(Optional.of(exam(20L, "20")));

        assertThatThrownBy(() -> service.create(
                new ResultRequest(1L, 5L, new BigDecimal("21"), SessionType.NORMAL, null, null)))
                .isInstanceOf(ResultConflictException.class);
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void createRejectsDuplicateResult() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(EnrollmentStatus.CONFIRMED)));
        when(resultRepository.existsByEnrollmentIdAndExamIdAndSessionType(1L, null, SessionType.NORMAL))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(
                new ResultRequest(1L, null, new BigDecimal("12"), SessionType.NORMAL, null, null)))
                .isInstanceOf(DuplicateResultException.class);
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void updateRejectsGradeAboveExamMax() {
        when(resultRepository.findById(100L)).thenReturn(Optional.of(result(ResultStatus.DRAFT)));
        when(examRepository.findById(5L)).thenReturn(Optional.of(exam(20L, "20")));

        assertThatThrownBy(() -> service.update(100L,
                new ResultUpdateRequest(new BigDecimal("25"), SessionType.NORMAL, ResultStatus.DRAFT, null)))
                .isInstanceOf(ResultConflictException.class);
        verify(resultRepository, never()).save(any(Result.class));
    }

    @Test
    void deleteRejectsPublishedResult() {
        Result published = result(ResultStatus.PUBLISHED);
        when(resultRepository.findById(100L)).thenReturn(Optional.of(published));

        assertThatThrownBy(() -> service.delete(100L))
                .isInstanceOf(ResultConflictException.class);
        verify(resultRepository, never()).delete(any(Result.class));
    }

    @Test
    void deleteRemovesDraftResult() {
        Result draft = result(ResultStatus.DRAFT);
        when(resultRepository.findById(100L)).thenReturn(Optional.of(draft));

        service.delete(100L);

        verify(resultRepository).delete(draft);
    }

    @Test
    void getThrowsNotFound() {
        when(resultRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(999L))
                .isInstanceOf(ResultNotFoundException.class);
    }

    @Test
    void listByEnrollmentThrowsWhenEnrollmentMissing() {
        when(enrollmentRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.listByEnrollment(1L))
                .isInstanceOf(EnrollmentNotFoundException.class);
    }

    @Test
    void listByEnrollmentReturnsResults() {
        when(enrollmentRepository.existsById(1L)).thenReturn(true);
        when(resultRepository.findByEnrollmentIdOrderByIdAsc(1L)).thenReturn(List.of(result(ResultStatus.DRAFT)));

        assertThat(service.listByEnrollment(1L)).hasSize(1);
    }
}