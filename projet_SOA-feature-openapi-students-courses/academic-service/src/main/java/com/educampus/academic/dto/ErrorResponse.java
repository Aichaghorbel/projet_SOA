package com.educampus.academic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Format unique de toutes les erreurs de l'API (schéma ErrorResponse du contrat).
 * Le champ details n'est présent que pour les erreurs de validation (400).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> details) {
}
