package com.educampus.academic.repository;

import com.educampus.academic.entity.Enrollment;
import com.educampus.academic.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment> {

    boolean existsByStudentIdAndCourseIdAndAcademicYear(Long studentId, Long courseId, String academicYear);

    /** Nombre d'inscriptions occupant une place (tout statut sauf celui donné, ex. CANCELLED). */
    long countByCourseIdAndAcademicYearAndStatusNot(Long courseId, String academicYear, EnrollmentStatus status);
}