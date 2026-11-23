package com.educampus.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Corps du POST /api/v1/enrollments. */
public record EnrollmentRequest(
        @NotNull @Min(1) Long studentId,
        @NotNull @Min(1) Long courseId,
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "doit avoir le format 2026-2027") String academicYear) {
}