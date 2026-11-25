package com.educampus.academic.dto;

import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.entity.SessionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Corps du PUT /api/v1/results/{id} (remplacement complet). */
public record ResultUpdateRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("20.0") BigDecimal grade,
        @NotNull SessionType sessionType,
        @NotNull ResultStatus status,
        @Size(max = 500) String comment) {
}