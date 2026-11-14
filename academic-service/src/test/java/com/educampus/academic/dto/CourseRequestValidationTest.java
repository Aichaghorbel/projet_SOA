package com.educampus.academic.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.educampus.academic.entity.Semester;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CourseRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private CourseRequest request(String code, Integer credits) {
        return new CourseRequest(code, "Architecture orientée services", "REST, gRPC et SOAP",
                credits, Semester.S5, "Dr. Karim Trabelsi", 40);
    }

    private boolean hasViolationOn(Set<ConstraintViolation<CourseRequest>> violations, String field) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void validRequestHasNoViolation() {
        assertThat(validator.validate(request("SOA301", 4))).isEmpty();
    }

    @Test
    void badCodeFormatIsRejected() {
        assertThat(hasViolationOn(validator.validate(request("soa-301", 4)), "code")).isTrue();
    }

    @Test
    void creditsOutOfRangeIsRejected() {
        assertThat(hasViolationOn(validator.validate(request("SOA301", 31)), "credits")).isTrue();
    }

    @Test
    void missingRequiredFieldsAreRejected() {
        var violations = validator.validate(new CourseRequest(null, null, null, null, null, null, null));
        assertThat(violations).hasSizeGreaterThanOrEqualTo(4);
    }
}
