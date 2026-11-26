package com.educampus.academic.dto;

import java.time.OffsetDateTime;

/** Réponse du POST /api/v1/certification-requests (schéma CertificationRequest du contrat). */
public record CertificationRequestResponse(
        String requestId,
        Long studentId,
        String certificationType,
        String status,
        OffsetDateTime submittedAt) {
}
