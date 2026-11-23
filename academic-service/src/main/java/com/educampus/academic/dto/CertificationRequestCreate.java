package com.educampus.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps du POST /api/v1/certification-requests. */
public record CertificationRequestCreate(
        @NotNull @Min(1) Long studentId,
        @NotBlank @Size(max = 100) String certificationType) {
}
