package com.educampus.academic.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.educampus.academic.entity.SessionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResultRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static <T> boolean hasViolationOn(Set<ConstraintViolation<T>> violations, String field) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    private ResultRequest request(Long enrollmentId, String grade, SessionType type, String comment) {
        return new ResultRequest(enrollmentId, 5L,
                grade == null ? null : new BigDecimal(grade), type, null, comment);
    }

    @Test
    void validRequestHasNoViolation() {
        assertThat(validator.validate(request(1L, "12.5", SessionType.NORMAL, "bien"))).isEmpty();
    }

    @Test
    void gradeAboveTwentyIsRejected() {
        var violations = validator.validate(request(1L, "20.5", SessionType.NORMAL, null));
        assertThat(hasViolationOn(violations, "grade")).isTrue();
    }

    @Test
    void negativeGradeIsRejected() {
        var violations = validator.validate(request(1L, "-1", SessionType.NORMAL, null));
        assertThat(hasViolationOn(violations, "grade")).isTrue();
    }

    @Test
    void missingSessionTypeIsRejected() {
        var violations = validator.validate(request(1L, "12", null, null));
        assertThat(hasViolationOn(violations, "sessionType")).isTrue();
    }

    @Test
    void nonPositiveEnrollmentIdIsRejected() {
        var violations = validator.validate(request(0L, "12", SessionType.NORMAL, null));
        assertThat(hasViolationOn(violations, "enrollmentId")).isTrue();
    }

    @Test
    void commentLongerThan500IsRejected() {
        var violations = validator.validate(request(1L, "12", SessionType.NORMAL, "x".repeat(501)));
        assertThat(hasViolationOn(violations, "comment")).isTrue();
    }

    @Test
    void updateWithoutStatusIsRejected() {
        var violations = validator.validate(
                new ResultUpdateRequest(new BigDecimal("12"), SessionType.NORMAL, null, null));
        assertThat(hasViolationOn(violations, "status")).isTrue();
    }
}