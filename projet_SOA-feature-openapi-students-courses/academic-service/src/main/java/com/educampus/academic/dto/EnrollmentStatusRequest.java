package com.educampus.academic.dto;

import com.educampus.academic.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

/** Corps du PATCH /api/v1/enrollments/{id}. */
public record EnrollmentStatusRequest(@NotNull EnrollmentStatus status) {
}