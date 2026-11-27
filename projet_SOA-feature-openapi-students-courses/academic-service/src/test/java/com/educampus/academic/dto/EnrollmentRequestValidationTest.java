package com.educampus.academic.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EnrollmentRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static <T> boolean hasViolationOn(Set<ConstraintViolation<T>> violations, String field) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void validRequestHasNoViolation() {
        assertThat(validator.validate(new EnrollmentRequest(10L, 20L, "2026-2027"))).isEmpty();
    }

    @Test
    void academicYearWrongFormatIsRejected() {
        var violations = validator.validate(new EnrollmentRequest(10L, 20L, "2026/2027"));
        assertThat(hasViolationOn(violations, "academicYear")).isTrue();
    }

    @Test
    void blankAcademicYearIsRejected() {
        var violations = validator.validate(new EnrollmentRequest(10L, 20L, " "));
        assertThat(hasViolationOn(violations, "academicYear")).isTrue();
    }

    @Test
    void missingStudentIsRejected() {
        var violations = validator.validate(new EnrollmentRequest(null, 20L, "2026-2027"));
        assertThat(hasViolationOn(violations, "studentId")).isTrue();
    }

    @Test
    void nonPositiveCourseIdIsRejected() {
        var violations = validator.validate(new EnrollmentRequest(10L, 0L, "2026-2027"));
        assertThat(hasViolationOn(violations, "courseId")).isTrue();
    }
}