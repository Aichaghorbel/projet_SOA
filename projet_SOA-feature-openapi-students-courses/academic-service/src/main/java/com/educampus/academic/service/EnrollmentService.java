package com.educampus.academic.service;

import com.educampus.academic.dto.EnrollmentRequest;
import com.educampus.academic.dto.EnrollmentResponse;
import com.educampus.academic.dto.PagedResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ResultRepository resultRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             ResultRepository resultRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.resultRepository = resultRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentResponse> list(Long studentId, Long courseId, EnrollmentStatus status,
                                                  String academicYear, int page, int size) {
        Specification<Enrollment> spec = Specification.where(null);
        if (studentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("studentId"), studentId));
        }
        if (courseId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("courseId"), courseId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (academicYear != null && !academicYear.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("academicYear"), academicYear));
        }
        return PagedResponse.from(enrollmentRepository
                .findAll(spec, PageRequest.of(page, size, Sort.by("id")))
                .map(EnrollmentResponse::from));
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse get(Long id) {
        return EnrollmentResponse.from(findOrThrow(id));
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        if (!studentRepository.existsById(request.studentId())) {
            throw new ResourceNotFoundException("Étudiant " + request.studentId() + " introuvable");
        }
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours " + request.courseId() + " introuvable"));

        if (enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicYear(
                request.studentId(), request.courseId(), request.academicYear())) {
            throw new DuplicateEnrollmentException(request.studentId(), request.courseId(), request.academicYear());
        }
        if (course.getCapacity() != null) {
            long taken = enrollmentRepository.countByCourseIdAndAcademicYearAndStatusNot(
                    request.courseId(), request.academicYear(), EnrollmentStatus.CANCELLED);
            if (taken >= course.getCapacity()) {
                throw new EnrollmentConflictException("Le cours " + course.getCode() + " est complet ("
                        + course.getCapacity() + " places)");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(request.studentId());
        enrollment.setCourseId(request.courseId());
        enrollment.setAcademicYear(request.academicYear());
        enrollment.setStatus(EnrollmentStatus.PENDING);
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    /** Une inscription CANCELLED ou COMPLETED est terminale : son statut ne change plus. */
    @Transactional
    public EnrollmentResponse updateStatus(Long id, EnrollmentStatus newStatus) {
        Enrollment enrollment = findOrThrow(id);
        EnrollmentStatus current = enrollment.getStatus();
        if (current == newStatus) {
            return EnrollmentResponse.from(enrollment);
        }
        if (current == EnrollmentStatus.CANCELLED || current == EnrollmentStatus.COMPLETED) {
            throw new EnrollmentConflictException("Impossible de passer une inscription " + current
                    + " au statut " + newStatus);
        }
        enrollment.setStatus(newStatus);
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = findOrThrow(id);
        if (resultRepository.existsByEnrollmentId(id)) {
            throw new EnrollmentConflictException(
                    "Impossible de supprimer l'inscription " + id + " : des résultats y sont rattachés");
        }
        enrollmentRepository.delete(enrollment);
    }

    private Enrollment findOrThrow(Long id) {
        return enrollmentRepository.findById(id).orElseThrow(() -> new EnrollmentNotFoundException(id));
    }
}