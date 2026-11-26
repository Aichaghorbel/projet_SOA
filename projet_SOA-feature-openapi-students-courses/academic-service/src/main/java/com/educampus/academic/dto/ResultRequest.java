package com.educampus.academic.dto;

import com.educampus.academic.entity.ResultStatus;
import com.educampus.academic.entity.SessionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Corps du POST /api/v1/results. {@code status} est optionnel (DRAFT par défaut). */
public record ResultRequest(
        @NotNull @Min(1) Long enrollmentId,
        @Min(1) Long examId,
        @NotNull @DecimalMin("0.0") @DecimalMax("20.0") BigDecimal grade,
        @NotNull SessionType sessionType,
        ResultStatus status,
        @Size(max = 500) String comment) {
}