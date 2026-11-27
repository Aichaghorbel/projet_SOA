package com.educampus.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.educampus.academic.dto.EnrollmentRequest;
import com.educampus.academic.dto.EnrollmentResponse;
import com.educampus.academic.entity.Course;
import com.educampus.academic.entity.Enrollment;
import com.educampus.academic.entity.EnrollmentStatus;
import com.educampus.academic.exception.DuplicateEnrollmentException;
import com.educampus.academic.exception.EnrollmentConflictException;
import com.educampus.academic.exception.EnrollmentNotFoundException;
import com.educampus.academic.exception.ResourceNotFoundException;
import com.educampus.academic.repository.CourseRepository;
import com.educampus.academic.repository.EnrollmentRepository;
import com.educampus.academic.repository.ResultRepository;
import com.educampus.academic.repository.StudentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private ResultRepository resultRepository;

    @InjectMocks
    private EnrollmentService service;

    private Course course(Integer capacity) {
        Course c = new Course();
        c.setCode("INF101");
        c.setCapacity(capacity);
        return c;
    }

    private Enrollment enrollment(Long id, EnrollmentStatus status) {
        Enrollment e = new Enrollment();
        e.setStudentId(10L);
        e.setCourseId(20L);
        e.setAcademicYear("2026-2027");
        e.setStatus(status);
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    private EnrollmentRequest request() {
        return new EnrollmentRequest(10L, 20L, "2026-2027");
    }

    @Test
    void createSavesPendingEnrollmentWhenCourseHasPlaces() {
        when(studentRepository.existsById(10L)).thenReturn(true);
        when(courseRepository.findById(20L)).thenReturn(Optional.of(course(30)));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicYear(10L, 20L, "2026-2027"))
                .thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndAcademicYearAndStatusNot(
                20L, "2026-2027", EnrollmentStatus.CANCELLED)).thenReturn(5L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse response = service.create(request());

        assertThat(response.status()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(response.studentId()).isEqualTo(10L);
        assertThat(response.courseId()).isEqualTo(20L);
    }

    @Test
    void createRejectsUnknownStudent() {
        when(studentRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void createRejectsUnknownCourse() {
        when(studentRepository.existsById(10L)).thenReturn(true);
        when(courseRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void createRejectsDuplicateEnrollment() {
        when(studentRepository.existsById(10L)).thenReturn(true);
        when(courseRepository.findById(20L)).thenReturn(Optional.of(course(30)));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicYear(10L, 20L, "2026-2027"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(DuplicateEnrollmentException.class);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void createRejectsFullCourse() {
        when(studentRepository.existsById(10L)).thenReturn(true);
        when(courseRepository.findById(20L)).thenReturn(Optional.of(course(2)));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicYear(10L, 20L, "2026-2027"))
                .thenReturn(false);
        when(enrollmentRepository.countByCourseIdAndAcademicYearAndStatusNot(
                20L, "2026-2027", EnrollmentStatus.CANCELLED)).thenReturn(2L);

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(EnrollmentConflictException.class)
                .hasMessageContaining("complet");
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void getThrowsNotFound() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(EnrollmentNotFoundException.class);
    }

    @Test
    void updateStatusToSameStatusDoesNotSave() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, EnrollmentStatus.CONFIRMED)));

        EnrollmentResponse response = service.updateStatus(1L, EnrollmentStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo(EnrollmentStatus.CONFIRMED);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void updateStatusChangesPendingToConfirmed() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, EnrollmentStatus.PENDING)));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse response = service.updateStatus(1L, EnrollmentStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    void updateStatusFromCancelledIsForbidden() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, EnrollmentStatus.CANCELLED)));

        assertThatThrownBy(() -> service.updateStatus(1L, EnrollmentStatus.CONFIRMED))
                .isInstanceOf(EnrollmentConflictException.class);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void deleteRejectsEnrollmentWithResults() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment(1L, EnrollmentStatus.CONFIRMED)));
        when(resultRepository.existsByEnrollmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(EnrollmentConflictException.class);
        verify(enrollmentRepository, never()).delete(any(Enrollment.class));
    }

    @Test
    void deleteRemovesEnrollmentWithoutResults() {
        Enrollment e = enrollment(1L, EnrollmentStatus.PENDING);
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(e));
        when(resultRepository.existsByEnrollmentId(1L)).thenReturn(false);

        service.delete(1L);

        verify(enrollmentRepository).delete(e);
    }
}