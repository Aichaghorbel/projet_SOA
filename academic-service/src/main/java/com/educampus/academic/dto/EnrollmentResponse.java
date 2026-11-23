package com.educampus.academic.dto;

import com.educampus.academic.entity.Enrollment;
import com.educampus.academic.entity.EnrollmentStatus;
import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        Long courseId,
        String academicYear,
        EnrollmentStatus status,
        LocalDateTime enrolledAt,
        LocalDateTime updatedAt) {

    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(e.getId(), e.getStudentId(), e.getCourseId(),
                e.getAcademicYear(), e.getStatus(), e.getEnrolledAt(), e.getUpdatedAt());
    }
}