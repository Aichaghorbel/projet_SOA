package com.educampus.academic.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StudentRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private StudentRequest request(String email, Integer level, LocalDate birth) {
        return new StudentRequest("ETU2026001", "Amine", "Ben Salah", email, birth,
                "+21650123456", "Génie Logiciel", level);
    }

    private boolean hasViolationOn(Set<ConstraintViolation<StudentRequest>> violations, String field) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void validRequestHasNoViolation() {
        var violations = validator.validate(
                request("amine.bensalah@educampus.tn", 3, LocalDate.of(2003, 5, 14)));
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidEmailIsRejected() {
        var violations = validator.validate(request("pas-un-email", 3, null));
        assertThat(hasViolationOn(violations, "email")).isTrue();
    }

    @Test
    void levelOutOfRangeIsRejected() {
        var violations = validator.validate(request("a@educampus.tn", 6, null));
        assertThat(hasViolationOn(violations, "level")).isTrue();
    }

    @Test
    void futureBirthDateIsRejected() {
        var violations = validator.validate(
                request("a@educampus.tn", 3, LocalDate.now().plusDays(1)));
        assertThat(hasViolationOn(violations, "dateOfBirth")).isTrue();
    }

    @Test
    void missingRequiredFieldsAreRejected() {
        var violations = validator.validate(
                new StudentRequest(null, null, null, null, null, null, null, null));
        assertThat(violations).hasSizeGreaterThanOrEqualTo(5);
    }
}
